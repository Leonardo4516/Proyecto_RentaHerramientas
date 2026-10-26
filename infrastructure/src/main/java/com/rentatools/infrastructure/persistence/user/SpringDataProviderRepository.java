package com.rentatools.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataProviderRepository extends JpaRepository<ProviderJpaEntity, Long> {
    Optional<ProviderJpaEntity> findByUserId(Long userId);
    Optional<ProviderJpaEntity> findByTaxId(String taxId);
    java.util.List<ProviderJpaEntity> findByVerified(Boolean verified);
}
