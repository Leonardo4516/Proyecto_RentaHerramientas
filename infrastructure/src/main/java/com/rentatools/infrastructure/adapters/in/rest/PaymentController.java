package com.rentatools.infrastructure.adapters.in.rest;

import com.rentatools.application.billing.ProcessPaymentUseCase;
import com.rentatools.application.billing.dto.PaymentResponse;
import com.rentatools.application.billing.dto.ProcessPaymentRequest;
import com.rentatools.domain.billing.port.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.PaymentStatus;

import com.rentatools.application.billing.RefundDepositUseCase;
import com.rentatools.application.billing.dto.RefundDepositRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagos", description = "Procesamiento de pagos y pasarela de simulación")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final RefundDepositUseCase refundDepositUseCase;
    private final PaymentRepository paymentRepository;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase,
                             RefundDepositUseCase refundDepositUseCase,
                             PaymentRepository paymentRepository) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.refundDepositUseCase = refundDepositUseCase;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todos los pagos", description = "Retorna el historial completo de pagos y transacciones registradas con filtros opcionales por método y estado")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) PaymentStatus status) {
        List<Payment> payments = paymentRepository.findAll();
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            payments = payments.stream()
                    .filter(p -> p.getPaymentMethod() != null && p.getPaymentMethod().name().equalsIgnoreCase(paymentMethod.trim()))
                    .toList();
        }
        if (status != null) {
            payments = payments.stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
        }
        List<PaymentResponse> list = payments.stream()
                .map(PaymentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Operation(summary = "Registrar y procesar pago", description = "Procesa el pago de una factura (Tarjeta, PSE, Transferencia) y actualiza su estado a PAGADA")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = processPaymentUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refund-deposit")
    @Operation(summary = "Reembolsar depósito de garantía", description = "Registra la devolución del depósito al cliente tras la liquidación de la inspección técnica (Estado: REEMBOLSADO)")
    public ResponseEntity<PaymentResponse> refundDeposit(@RequestBody RefundDepositRequest request) {
        PaymentResponse response = refundDepositUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Consultar pago por factura", description = "Obtiene los detalles del pago asociado a una factura")
    public ResponseEntity<PaymentResponse> getPaymentByInvoice(@PathVariable Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId)
                .map(PaymentResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Consultar pagos por cliente", description = "Obtiene el historial de pagos realizados por un cliente")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByClient(@PathVariable Long clientId) {
        List<PaymentResponse> list = paymentRepository.findByClientId(clientId).stream()
                .map(PaymentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
