package com.rentatools.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataClientRepository extends JpaRepository<ClientJpaEntity, UUID> {
    Optional<ClientJpaEntity> findByUserId(UUID userId);
    Optional<ClientJpaEntity> findByDocumentNumber(String documentNumber);
}
