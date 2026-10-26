package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "return_damages")
public class ReturnDamageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rental_return_id", insertable = false, updatable = false)
    private Long rentalReturnId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "penalty_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal penaltyFee = BigDecimal.ZERO;

    @Column(name = "evidence_url", columnDefinition = "TEXT")
    private String evidenceUrl;

    @Column(name = "reported_at")
    private Instant reportedAt = Instant.now();

    public ReturnDamageEntity() {}

    public ReturnDamageEntity(String description, BigDecimal penaltyFee, String evidenceUrl, Instant reportedAt) {
        this.description = description;
        this.penaltyFee = penaltyFee != null ? penaltyFee : BigDecimal.ZERO;
        this.evidenceUrl = evidenceUrl;
        this.reportedAt = reportedAt != null ? reportedAt : Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRentalReturnId() { return rentalReturnId; }
    public void setRentalReturnId(Long rentalReturnId) { this.rentalReturnId = rentalReturnId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPenaltyFee() { return penaltyFee; }
    public void setPenaltyFee(BigDecimal penaltyFee) { this.penaltyFee = penaltyFee; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public Instant getReportedAt() { return reportedAt; }
    public void setReportedAt(Instant reportedAt) { this.reportedAt = reportedAt; }
}
