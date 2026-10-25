package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.ToolReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataToolReviewRepository extends JpaRepository<ToolReviewEntity, Long> {
    List<ToolReviewEntity> findByToolIdOrderByCreatedAtDesc(Long toolId);
    List<ToolReviewEntity> findByClientId(Long clientId);
    boolean existsByRentalId(Long rentalId);
}
