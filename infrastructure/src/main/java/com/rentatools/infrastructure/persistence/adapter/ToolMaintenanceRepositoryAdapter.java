package com.rentatools.infrastructure.persistence.adapter;

import com.rentatools.domain.tool.ToolMaintenanceLog;
import com.rentatools.domain.tool.ToolMaintenanceRepository;
import com.rentatools.infrastructure.persistence.entity.ToolMaintenanceLogEntity;
import com.rentatools.infrastructure.persistence.repository.ToolMaintenanceLogJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolMaintenanceRepositoryAdapter implements ToolMaintenanceRepository {

    private final ToolMaintenanceLogJpaRepository jpaRepository;

    public ToolMaintenanceRepositoryAdapter(ToolMaintenanceLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ToolMaintenanceLog save(ToolMaintenanceLog log) {
        ToolMaintenanceLogEntity entity = new ToolMaintenanceLogEntity(
                log.getToolId(),
                log.getMaintenanceType(),
                log.getDescription(),
                log.getTechnicianName(),
                log.getCost(),
                log.getPerformedAt(),
                log.getNextMaintenanceDate(),
                log.getNotes()
        );
        if (log.getId() != null) {
            entity.setId(log.getId());
        }
        ToolMaintenanceLogEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ToolMaintenanceLog> findByToolId(Long toolId) {
        return jpaRepository.findByToolIdOrderByPerformedAtDesc(toolId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ToolMaintenanceLog toDomain(ToolMaintenanceLogEntity e) {
        return new ToolMaintenanceLog(
                e.getId(),
                e.getToolId(),
                e.getMaintenanceType(),
                e.getDescription(),
                e.getTechnicianName(),
                e.getCost(),
                e.getPerformedAt(),
                e.getNextMaintenanceDate(),
                e.getNotes()
        );
    }
}
