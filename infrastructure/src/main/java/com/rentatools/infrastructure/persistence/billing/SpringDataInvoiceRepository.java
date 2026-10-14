package com.rentatools.infrastructure.persistence.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceJpaEntity, Long> {
    Optional<InvoiceJpaEntity> findByInvoiceNumber(String invoiceNumber);
    Optional<InvoiceJpaEntity> findByRentalId(Long rentalId);
    List<InvoiceJpaEntity> findByClientId(Long clientId);
}
