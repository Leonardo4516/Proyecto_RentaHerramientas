package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.ToolCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ToolCategoryJpaRepository extends JpaRepository<ToolCategoryEntity, UUID> {
    List<ToolCategoryEntity> findByActiveTrue();
}
