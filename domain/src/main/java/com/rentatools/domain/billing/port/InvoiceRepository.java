package com.rentatools.domain.billing.port;

import com.rentatools.domain.billing.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID id);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByRentalId(UUID rentalId);
    List<Invoice> findByClientId(UUID clientId);
    List<Invoice> findAll();
    long count();
}
