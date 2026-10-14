package com.rentatools.infrastructure.persistence.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceJpaEntity, UUID> {
    Optional<InvoiceJpaEntity> findByInvoiceNumber(String invoiceNumber);
    Optional<InvoiceJpaEntity> findByRentalId(UUID rentalId);
    Optional<InvoiceJpaEntity> findByPaymentId(UUID paymentId);

    @Query("SELECT i FROM InvoiceJpaEntity i, RentalEntity r WHERE i.rentalId = r.id AND r.clientId = :clientId")
    List<InvoiceJpaEntity> findByClientId(@Param("clientId") UUID clientId);
}
