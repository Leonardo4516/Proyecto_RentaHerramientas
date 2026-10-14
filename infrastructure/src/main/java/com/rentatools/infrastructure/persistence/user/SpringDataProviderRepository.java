package com.rentatools.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataProviderRepository extends JpaRepository<ProviderJpaEntity, UUID> {
    Optional<ProviderJpaEntity> findByUserId(UUID userId);
    Optional<ProviderJpaEntity> findByTaxId(String taxId);
}
