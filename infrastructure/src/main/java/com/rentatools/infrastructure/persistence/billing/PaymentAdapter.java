package com.rentatools.infrastructure.persistence.billing;

import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.port.PaymentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentAdapter implements PaymentRepository {
    private final SpringDataPaymentRepository paymentRepo;

    public PaymentAdapter(SpringDataPaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public Payment save(Payment payment) {
        if (payment == null) return null;
        if (payment.getId() == null) {
            payment.setId(UUID.randomUUID());
        }
        PaymentJpaEntity entity = toEntity(payment);
        PaymentJpaEntity saved = paymentRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByInvoiceId(UUID invoiceId) {
        return paymentRepo.findByInvoiceId(invoiceId).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByRentalId(UUID rentalId) {
        return paymentRepo.findByRentalId(rentalId).map(this::toDomain);
    }

    @Override
    public List<Payment> findByClientId(UUID clientId) {
        return paymentRepo.findByClientId(clientId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private PaymentJpaEntity toEntity(Payment p) {
        if (p == null) return null;
        PaymentJpaEntity e = new PaymentJpaEntity();
        e.setId(p.getId() != null ? p.getId() : UUID.randomUUID());
        e.setRentalId(p.getRentalId());
        e.setAmount(p.getAmount());
        e.setPaymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod() : "Tarjeta de Crédito");
        e.setStatus(p.getStatus() != null ? p.getStatus() : "PAGADO");
        e.setTransactionReference(p.getTransactionReference() != null ? p.getTransactionReference() : "TRX-" + UUID.randomUUID().toString().substring(0, 8));
        e.setPaidAt(p.getPaidAt() != null ? p.getPaidAt() : Instant.now());
        return e;
    }

    private Payment toDomain(PaymentJpaEntity e) {
        if (e == null) return null;
        Payment p = new Payment();
        p.setId(e.getId());
        p.setRentalId(e.getRentalId());
        p.setAmount(e.getAmount());
        p.setPaymentMethod(e.getPaymentMethod());
        p.setStatus(e.getStatus());
        p.setTransactionReference(e.getTransactionReference());
        p.setPaidAt(e.getPaidAt());
        return p;
    }
}
