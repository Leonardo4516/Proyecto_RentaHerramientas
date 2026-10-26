package com.rentatools.domain.billing.port;

import com.rentatools.domain.billing.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByRentalId(Long rentalId);
    List<Invoice> findByClientId(Long clientId);
    List<Invoice> findAll();
    long count();
    default List<Invoice> findOverdue(java.time.LocalDateTime now) {
        return List.of();
    }
}
