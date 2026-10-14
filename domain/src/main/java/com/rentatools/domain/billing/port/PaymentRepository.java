package com.rentatools.domain.billing.port;

import com.rentatools.domain.billing.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByInvoiceId(Long invoiceId);
    Optional<Payment> findByRentalId(Long rentalId);
    List<Payment> findByClientId(Long clientId);
    List<Payment> findAll();
}
