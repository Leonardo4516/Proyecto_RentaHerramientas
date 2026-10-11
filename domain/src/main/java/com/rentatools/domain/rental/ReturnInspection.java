package com.rentatools.domain.rental;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReturnInspection {
    private final UUID id;
    private final Instant returnDate;
    private final UUID inspectorUserId;
    private final String equipmentCondition;
    private final boolean hasDamage;
    private final BigDecimal depositRefunded;
    private final String notes;
    private final List<DamageReport> damages;

    public ReturnInspection(UUID id, Instant returnDate, UUID inspectorUserId, String equipmentCondition,
                            boolean hasDamage, BigDecimal depositRefunded, String notes, List<DamageReport> damages) {
        this.id = id != null ? id : UUID.randomUUID();
        this.returnDate = returnDate != null ? returnDate : Instant.now();
        this.inspectorUserId = inspectorUserId;
        this.equipmentCondition = equipmentCondition;
        this.hasDamage = hasDamage;
        this.depositRefunded = depositRefunded != null ? depositRefunded : BigDecimal.ZERO;
        this.notes = notes;
        this.damages = damages != null ? new ArrayList<>(damages) : new ArrayList<>();
    }

    public UUID getId() { return id; }
    public Instant getReturnDate() { return returnDate; }
    public UUID getInspectorUserId() { return inspectorUserId; }
    public String getEquipmentCondition() { return equipmentCondition; }
    public boolean isHasDamage() { return hasDamage; }
    public BigDecimal getDepositRefunded() { return depositRefunded; }
    public String getNotes() { return notes; }
    public List<DamageReport> getDamages() { return Collections.unmodifiableList(damages); }
}
