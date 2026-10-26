package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tool_maintenance_logs")
public class ToolMaintenanceLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "maintenance_type", nullable = false, length = 50)
    private String maintenanceType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "technician_name", length = 100)
    private String technicianName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "performed_at")
    private Instant performedAt = Instant.now();

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public ToolMaintenanceLogEntity() {}

    public ToolMaintenanceLogEntity(Long toolId, String maintenanceType, String description,
                                    String technicianName, BigDecimal cost, Instant performedAt,
                                    LocalDate nextMaintenanceDate, String notes) {
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
    public void setId(Long id) { this.id = id; }

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public Instant getPerformedAt() { return performedAt; }
    public void setPerformedAt(Instant performedAt) { this.performedAt = performedAt; }

    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
