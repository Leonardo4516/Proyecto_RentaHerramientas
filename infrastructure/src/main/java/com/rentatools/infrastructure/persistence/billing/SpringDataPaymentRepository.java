package com.rentatools.infrastructure.persistence.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
    Optional<PaymentJpaEntity> findByInvoiceId(Long invoiceId);
    Optional<PaymentJpaEntity> findByRentalId(Long rentalId);
    List<PaymentJpaEntity> findByClientId(Long clientId);
}
