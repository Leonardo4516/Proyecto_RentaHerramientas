package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.ToolMaintenanceLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolMaintenanceLogJpaRepository extends JpaRepository<ToolMaintenanceLogEntity, Long> {
    List<ToolMaintenanceLogEntity> findByToolIdOrderByPerformedAtDesc(Long toolId);
}
