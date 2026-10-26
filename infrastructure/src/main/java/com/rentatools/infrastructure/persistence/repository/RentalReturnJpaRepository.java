package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.RentalReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalReturnJpaRepository extends JpaRepository<RentalReturnEntity, Long> {
    Optional<RentalReturnEntity> findByRentalId(Long rentalId);
    boolean existsByRentalId(Long rentalId);
}
