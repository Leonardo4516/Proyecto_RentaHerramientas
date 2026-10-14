package com.rentatools.infrastructure.persistence.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    Optional<PaymentJpaEntity> findByRentalId(UUID rentalId);
    Optional<PaymentJpaEntity> findByTransactionReference(String transactionReference);

    @Query("SELECT p FROM PaymentJpaEntity p, InvoiceJpaEntity i WHERE i.paymentId = p.id AND i.id = :invoiceId")
    Optional<PaymentJpaEntity> findByInvoiceId(@Param("invoiceId") UUID invoiceId);

    @Query("SELECT p FROM PaymentJpaEntity p, RentalEntity r WHERE p.rentalId = r.id AND r.clientId = :clientId")
    List<PaymentJpaEntity> findByClientId(@Param("clientId") UUID clientId);
}
