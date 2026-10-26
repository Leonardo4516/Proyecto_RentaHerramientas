package com.rentatools.infrastructure.persistence.billing;

import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.PaymentMethod;
import com.rentatools.domain.billing.PaymentStatus;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.infrastructure.persistence.user.ClientJpaEntity;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentAdapter implements PaymentRepository {
    private final SpringDataPaymentRepository paymentRepo;
    private final SpringDataClientRepository clientRepo;

    public PaymentAdapter(SpringDataPaymentRepository paymentRepo, SpringDataClientRepository clientRepo) {
        this.paymentRepo = paymentRepo;
        this.clientRepo = clientRepo;
    }

    private Long resolveClientId(Long id) {
        if (id == null) return null;
        if (clientRepo.existsById(id)) return id;
        return clientRepo.findByUserId(id).map(ClientJpaEntity::getId).orElse(id);
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
        Long resolved = resolveClientId(clientId);
        return paymentRepo.findByClientId(resolved).stream().map(this::toDomain).collect(Collectors.toList());
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
        e.setClientId(resolveClientId(p.getClientId()));
        e.setAmount(p.getAmount());
        e.setPaymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "CREDIT_CARD");

        String statusStr = "PAGADO";
        if (p.getStatus() != null) {
            statusStr = switch (p.getStatus()) {
                case COMPLETED -> "PAGADO";
                case PENDING -> "PENDIENTE";
                case FAILED -> "RECHAZADO";
                case REFUNDED -> "REEMBOLSADO";
            };
        }
        e.setStatus(statusStr);
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

        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        if (e.getPaymentMethod() != null) {
            String m = e.getPaymentMethod().toUpperCase();
            if (m.contains("CREDIT") || m.contains("CRÉDITO")) {
                method = PaymentMethod.CREDIT_CARD;
            } else if (m.contains("DEBIT") || m.contains("DÉBITO")) {
                method = PaymentMethod.DEBIT_CARD;
            } else if (m.contains("PSE")) {
                method = PaymentMethod.PSE;
            } else if (m.contains("TRANSFER")) {
                method = PaymentMethod.BANK_TRANSFER;
            } else {
                try {
                    method = PaymentMethod.valueOf(m);
                } catch (Exception ex) {
                    method = PaymentMethod.CREDIT_CARD;
                }
            }
        }
        p.setPaymentMethod(method);

        PaymentStatus status = switch (e.getStatus() != null ? e.getStatus().toUpperCase() : "PAGADO") {
            case "PAGADO", "COMPLETED" -> PaymentStatus.COMPLETED;
            case "PENDIENTE", "PENDING" -> PaymentStatus.PENDING;
            case "RECHAZADO", "FAILED" -> PaymentStatus.FAILED;
            case "REEMBOLSADO", "REFUNDED" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.COMPLETED;
        };
        p.setStatus(status);

        p.setTransactionReference(e.getTransactionReference());
        p.setPaymentDate(e.getPaymentDate());
        p.setNotes(e.getNotes());
        return p;
    }
}
