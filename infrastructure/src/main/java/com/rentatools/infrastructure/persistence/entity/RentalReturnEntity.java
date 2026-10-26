package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_returns")
public class RentalReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rental_id", unique = true, nullable = false)
    private Long rentalId;

    @Column(name = "return_date")
    private Instant returnDate = Instant.now();

    @Column(name = "inspector_user_id")
    private Long inspectorUserId;

    @Column(name = "equipment_condition", nullable = false, length = 50)
    private String equipmentCondition;

    @Column(name = "has_damage")
    private Boolean hasDamage = false;

    @Column(name = "deposit_refunded", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositRefunded = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "rental_return_id", nullable = false)
    private List<ReturnDamageEntity> damages = new ArrayList<>();

    public RentalReturnEntity() {}

    public RentalReturnEntity(Long rentalId, Instant returnDate, Long inspectorUserId,
                              String equipmentCondition, Boolean hasDamage,
                              BigDecimal depositRefunded, String notes) {
        this.rentalId = rentalId;
        this.returnDate = returnDate != null ? returnDate : Instant.now();
        this.inspectorUserId = inspectorUserId;
        this.equipmentCondition = equipmentCondition;
        this.hasDamage = hasDamage != null ? hasDamage : false;
        this.depositRefunded = depositRefunded != null ? depositRefunded : BigDecimal.ZERO;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }

    public Instant getReturnDate() { return returnDate; }
    public void setReturnDate(Instant returnDate) { this.returnDate = returnDate; }

    public Long getInspectorUserId() { return inspectorUserId; }
    public void setInspectorUserId(Long inspectorUserId) { this.inspectorUserId = inspectorUserId; }

    public String getEquipmentCondition() { return equipmentCondition; }
    public void setEquipmentCondition(String equipmentCondition) { this.equipmentCondition = equipmentCondition; }

    public Boolean getHasDamage() { return hasDamage; }
    public void setHasDamage(Boolean hasDamage) { this.hasDamage = hasDamage; }

    public BigDecimal getDepositRefunded() { return depositRefunded; }
    public void setDepositRefunded(BigDecimal depositRefunded) { this.depositRefunded = depositRefunded; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<ReturnDamageEntity> getDamages() { return damages; }
    public void setDamages(List<ReturnDamageEntity> damages) {
        this.damages = damages != null ? damages : new ArrayList<>();
    }
}
