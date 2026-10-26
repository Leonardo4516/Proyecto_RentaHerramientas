package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.PaymentResponse;
import com.rentatools.application.billing.dto.RefundDepositRequest;
import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.PaymentMethod;
import com.rentatools.domain.billing.PaymentStatus;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.common.exception.ResourceNotFoundException;
import com.rentatools.domain.notification.port.NotificationPort;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.ReturnInspection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundDepositUseCase {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final NotificationPort notificationPort;

    public RefundDepositUseCase(PaymentRepository paymentRepository,
                                RentalRepository rentalRepository,
                                NotificationPort notificationPort) {
        this.paymentRepository = paymentRepository;
        this.rentalRepository = rentalRepository;
        this.notificationPort = notificationPort;
    }

    public PaymentResponse execute(RefundDepositRequest request) {
        if (request == null || request.getRentalId() == null) {
            throw new BusinessException("El ID del alquiler es obligatorio para procesar el reembolso");
        }

        RentalBooking rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler no encontrado con ID: " + request.getRentalId()));

        BigDecimal amount = request.getAmount();
        if (amount == null) {
            // Obtener el depósito reembolsable de la inspección o de la reserva
            amount = rentalRepository.findInspectionByRentalId(rental.getId())
                    .map(ReturnInspection::getDepositRefunded)
                    .orElseGet(rental::getDepositAmount);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto a reembolsar debe ser mayor a 0. Monto liquidado: " + amount);
        }

        PaymentMethod method = PaymentMethod.BANK_TRANSFER;
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            try {
                method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                method = PaymentMethod.BANK_TRANSFER;
            }
        }

        Payment payment = new Payment();
        payment.setRentalId(rental.getId());
        payment.setClientId(rental.getClientId());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setTransactionReference("REFUND-" + rental.getId() + "-" + System.currentTimeMillis());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setNotes(request.getNotes() != null && !request.getNotes().isBlank()
                ? request.getNotes()
                : "Reembolso oficial de depósito de garantía liquidado tras inspección técnica");

        Payment saved = paymentRepository.save(payment);

        if (notificationPort != null) {
            notificationPort.sendToClient(rental.getClientId(), "/topic/client/" + rental.getClientId(),
                    "Tu reembolso de depósito por $" + saved.getAmount() + " para el alquiler #" + rental.getId() +
                            " ha sido procesado exitosamente (Ref: " + saved.getTransactionReference() + ")");
        }

        return PaymentResponse.fromDomain(saved);
    }
}
