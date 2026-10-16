package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.ToolEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
@Repository
public interface ToolJpaRepository extends JpaRepository<ToolEntity, Long> {
    Optional<ToolEntity> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ToolEntity t WHERE t.id = :id")
    Optional<ToolEntity> findByIdWithPessimisticLock(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findByProviderId(Long providerId);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findByStatus(String status);

    @EntityGraph(attributePaths = {"category"})
    List<ToolEntity> findAll();
}
