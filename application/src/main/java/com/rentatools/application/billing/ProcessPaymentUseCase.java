package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.PaymentResponse;
import com.rentatools.application.billing.dto.ProcessPaymentRequest;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.PaymentMethod;
import com.rentatools.domain.billing.PaymentStatus;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.common.exception.ResourceNotFoundException;
import com.rentatools.domain.notification.port.NotificationPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public class ProcessPaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationPort notificationPort;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository, NotificationPort notificationPort) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.notificationPort = notificationPort;
    }

    public PaymentResponse execute(ProcessPaymentRequest request) {
        if (request == null) {
            throw new BusinessException("Datos de pago requeridos");
        }

        if (request.getInvoiceId() == null) {
            throw new BusinessException("El ID de la factura es obligatorio");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + request.getInvoiceId()));

        if (invoice.isPaid()) {
            throw new BusinessException("La factura ya ha sido pagada previamente");
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : invoice.getTotalAmount();
        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        if (request.getPaymentMethod() != null) {
            try {
                method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                method = PaymentMethod.CREDIT_CARD;
            }
        }

        String reference = request.getTransactionReference() != null ? request.getTransactionReference() : "TXN-" + System.currentTimeMillis();

        Payment payment = new Payment();
        payment.setInvoiceId(invoice.getId());
        payment.setRentalId(invoice.getRentalId());
        payment.setClientId(invoice.getClientId());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionReference(reference);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setNotes(request.getNotes() != null ? request.getNotes() : "Pago procesado exitosamente vía pasarela simulada");

        Payment savedPayment = paymentRepository.save(payment);

        invoice.setPaid(true);
        invoice.setPaymentDate(savedPayment.getPaymentDate());
        invoiceRepository.save(invoice);

        if (notificationPort != null) {
            notificationPort.sendToClient(invoice.getClientId(), "/topic/client/" + invoice.getClientId(),
                    "Tu pago de $" + savedPayment.getAmount() + " para la factura " + invoice.getInvoiceNumber() + " fue confirmado con referencia " + savedPayment.getTransactionReference());
            notificationPort.broadcast("/topic/catalog", "Nueva renta activada para alquiler #" + invoice.getRentalId());
        }

        return PaymentResponse.fromDomain(savedPayment);
    }
}
