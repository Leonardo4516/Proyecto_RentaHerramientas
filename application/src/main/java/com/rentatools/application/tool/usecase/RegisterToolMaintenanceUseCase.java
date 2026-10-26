package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.RegisterMaintenanceCommand;
import com.rentatools.application.tool.dto.ToolMaintenanceResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolMaintenanceLog;
import com.rentatools.domain.tool.ToolMaintenanceRepository;
import com.rentatools.domain.tool.ToolRepository;

import java.time.Instant;

public class RegisterToolMaintenanceUseCase {

    private final ToolMaintenanceRepository maintenanceRepository;
    private final ToolRepository toolRepository;

    public RegisterToolMaintenanceUseCase(ToolMaintenanceRepository maintenanceRepository, ToolRepository toolRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.toolRepository = toolRepository;
    }

    public ToolMaintenanceResponse execute(Long toolId, RegisterMaintenanceCommand command) {
        if (toolId == null) {
            throw new BusinessRuleValidationException("El ID de la herramienta es obligatorio.");
        }
        if (command == null || command.description() == null || command.description().isBlank()) {
            throw new BusinessRuleValidationException("La descripción del mantenimiento es obligatoria.");
        }

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + toolId));

        ToolMaintenanceLog log = new ToolMaintenanceLog(
                null,
                tool.getId(),
                command.maintenanceType() != null && !command.maintenanceType().isBlank() ? command.maintenanceType() : "PREVENTIVO",
                command.description().trim(),
                command.technicianName(),
                command.cost(),
                Instant.now(),
                command.nextMaintenanceDate(),
                command.notes()
        );

        ToolMaintenanceLog saved = maintenanceRepository.save(log);

        if (Boolean.TRUE.equals(command.markToolInMaintenance())) {
            tool.sendToMaintenance();
            toolRepository.save(tool);
        }

        return new ToolMaintenanceResponse(
                saved.getId(),
                saved.getToolId(),
                saved.getMaintenanceType(),
                saved.getDescription(),
                saved.getTechnicianName(),
                saved.getCost(),
                saved.getPerformedAt(),
                saved.getNextMaintenanceDate(),
                saved.getNotes()
        );
    }
}
