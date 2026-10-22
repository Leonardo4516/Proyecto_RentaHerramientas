package com.rentatools.application.tool.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterMaintenanceCommand(
        String maintenanceType,
        String description,
        String technicianName,
        BigDecimal cost,
        LocalDate nextMaintenanceDate,
        String notes,
        Boolean markToolInMaintenance
) {
    public RegisterMaintenanceCommand {
        if (maintenanceType == null || maintenanceType.isBlank()) {
            throw new IllegalArgumentException("El tipo de mantenimiento es obligatorio");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripción técnica es obligatoria");
        }
        if (cost == null) {
            cost = BigDecimal.ZERO;
        }
        if (markToolInMaintenance == null) {
            markToolInMaintenance = false;
        }
    }
}
