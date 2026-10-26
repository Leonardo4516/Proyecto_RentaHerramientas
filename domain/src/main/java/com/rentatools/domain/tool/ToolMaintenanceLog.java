package com.rentatools.domain.tool;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ToolMaintenanceLog {
    private final Long id;
    private final Long toolId;
    private final String maintenanceType;
    private final String description;
    private final String technicianName;
    private final BigDecimal cost;
    private final Instant performedAt;
    private final LocalDate nextMaintenanceDate;
    private final String notes;

    public ToolMaintenanceLog(Long id, Long toolId, String maintenanceType, String description,
                              String technicianName, BigDecimal cost, Instant performedAt,
                              LocalDate nextMaintenanceDate, String notes) {
        this.id = id != null ? id : null;
        this.toolId = toolId;
        this.maintenanceType = maintenanceType;
        this.description = description;
        this.technicianName = technicianName;
        this.cost = cost != null ? cost : BigDecimal.ZERO;
        this.performedAt = performedAt != null ? performedAt : Instant.now();
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Long getToolId() { return toolId; }
    public String getMaintenanceType() { return maintenanceType; }
    public String getDescription() { return description; }
    public String getTechnicianName() { return technicianName; }
    public BigDecimal getCost() { return cost; }
    public Instant getPerformedAt() { return performedAt; }
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public String getNotes() { return notes; }
}
