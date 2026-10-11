package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.ToolEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToolJpaRepository extends JpaRepository<ToolEntity, UUID> {
    Optional<ToolEntity> findByCode(String code);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findByCategoryId(UUID categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findByProviderId(UUID providerId);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findByStatus(String status);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findAll();
}
