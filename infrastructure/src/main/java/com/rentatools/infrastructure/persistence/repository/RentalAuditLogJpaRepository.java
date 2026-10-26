package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.RentalAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalAuditLogJpaRepository extends JpaRepository<RentalAuditLogEntity, Long> {
    List<RentalAuditLogEntity> findByRentalIdOrderByCreatedAtAsc(Long rentalId);
}
