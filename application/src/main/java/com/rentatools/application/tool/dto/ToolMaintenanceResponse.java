package com.rentatools.application.tool.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ToolMaintenanceResponse(
        Long id,
        Long toolId,
        String maintenanceType,
        String description,
        String technicianName,
        BigDecimal cost,
        Instant performedAt,
        LocalDate nextMaintenanceDate,
        String notes
) {}
