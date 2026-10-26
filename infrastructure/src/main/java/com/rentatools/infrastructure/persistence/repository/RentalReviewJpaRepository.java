package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.RentalReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalReviewJpaRepository extends JpaRepository<RentalReviewEntity, Long> {
    Optional<RentalReviewEntity> findByRentalId(Long rentalId);
    boolean existsByRentalId(Long rentalId);
    List<RentalReviewEntity> findByToolId(Long toolId);
    List<RentalReviewEntity> findByClientId(Long clientId);
}
