package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.PromoCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPromoCouponRepository extends JpaRepository<PromoCouponEntity, Long> {
    Optional<PromoCouponEntity> findByCodeIgnoreCaseAndIsActiveTrue(String code);
}
