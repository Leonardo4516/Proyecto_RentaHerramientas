package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.ToolMaintenanceResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.tool.ToolMaintenanceRepository;

import java.util.List;

public class GetToolMaintenanceHistoryUseCase {

    private final ToolMaintenanceRepository maintenanceRepository;

    public GetToolMaintenanceHistoryUseCase(ToolMaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    public List<ToolMaintenanceResponse> execute(Long toolId) {
        if (toolId == null) {
            throw new BusinessRuleValidationException("El ID de la herramienta es obligatorio.");
        }

        return maintenanceRepository.findByToolId(toolId).stream()
                .map(m -> new ToolMaintenanceResponse(
                        m.getId(),
                        m.getToolId(),
                        m.getMaintenanceType(),
                        m.getDescription(),
                        m.getTechnicianName(),
                        m.getCost(),
                        m.getPerformedAt(),
                        m.getNextMaintenanceDate(),
                        m.getNotes()
                ))
                .toList();
    }
}
