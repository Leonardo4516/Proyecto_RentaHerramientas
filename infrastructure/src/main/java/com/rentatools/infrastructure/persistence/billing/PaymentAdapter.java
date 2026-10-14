package com.rentatools.infrastructure.persistence.billing;

import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.port.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentAdapter implements PaymentRepository {
    private final SpringDataPaymentRepository paymentRepo;

    public PaymentAdapter(SpringDataPaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = toEntity(payment);
        PaymentJpaEntity saved = paymentRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByInvoiceId(Long invoiceId) {
        return paymentRepo.findByInvoiceId(invoiceId).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByRentalId(Long rentalId) {
        return paymentRepo.findByRentalId(rentalId).map(this::toDomain);
    }

    @Override
    public List<Payment> findByClientId(Long clientId) {
        return paymentRepo.findByClientId(clientId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private PaymentJpaEntity toEntity(Payment p) {
        if (p == null) return null;
        PaymentJpaEntity e = new PaymentJpaEntity();
        e.setId(p.getId());
        e.setInvoiceId(p.getInvoiceId());
        e.setRentalId(p.getRentalId());
        e.setClientId(p.getClientId());
        e.setAmount(p.getAmount());
        e.setPaymentMethod(p.getPaymentMethod());
        e.setStatus(p.getStatus());
        e.setTransactionReference(p.getTransactionReference());
        e.setPaymentDate(p.getPaymentDate());
        e.setNotes(p.getNotes());
        return e;
    }

    private Payment toDomain(PaymentJpaEntity e) {
        if (e == null) return null;
        Payment p = new Payment();
        p.setId(e.getId());
        p.setInvoiceId(e.getInvoiceId());
        p.setRentalId(e.getRentalId());
        p.setClientId(e.getClientId());
        p.setAmount(e.getAmount());
        p.setPaymentMethod(e.getPaymentMethod());
        p.setStatus(e.getStatus());
        p.setTransactionReference(e.getTransactionReference());
        p.setPaymentDate(e.getPaymentDate());
        p.setNotes(e.getNotes());
        return p;
    }
}
