package com.rentatools.domain.billing.port;

import com.rentatools.domain.billing.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
    Optional<Payment> findByInvoiceId(UUID invoiceId);
    Optional<Payment> findByRentalId(UUID rentalId);
    List<Payment> findByClientId(UUID clientId);
    List<Payment> findAll();
}
