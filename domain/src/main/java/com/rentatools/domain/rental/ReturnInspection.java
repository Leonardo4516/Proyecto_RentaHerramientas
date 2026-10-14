package com.rentatools.domain.rental;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ReturnInspection {
    private final Long id;
    private final Instant returnDate;
    private final Long inspectorUserId;
    private final String equipmentCondition;
    private final boolean hasDamage;
    private final BigDecimal depositRefunded;
    private final String notes;
    private final List<DamageReport> damages;

    public ReturnInspection(Long id, Instant returnDate, Long inspectorUserId, String equipmentCondition,
                            boolean hasDamage, BigDecimal depositRefunded, String notes, List<DamageReport> damages) {
        this.id = id != null ? id : null;
        this.returnDate = returnDate != null ? returnDate : Instant.now();
        this.inspectorUserId = inspectorUserId;
        this.equipmentCondition = equipmentCondition;
        this.hasDamage = hasDamage;
        this.depositRefunded = depositRefunded != null ? depositRefunded : BigDecimal.ZERO;
        this.notes = notes;
        this.damages = damages != null ? new ArrayList<>(damages) : new ArrayList<>();
    }

    public Long getId() { return id; }
    public Instant getReturnDate() { return returnDate; }
    public Long getInspectorUserId() { return inspectorUserId; }
    public String getEquipmentCondition() { return equipmentCondition; }
    public boolean isHasDamage() { return hasDamage; }
    public BigDecimal getDepositRefunded() { return depositRefunded; }
    public String getNotes() { return notes; }
    public List<DamageReport> getDamages() { return Collections.unmodifiableList(damages); }
}
