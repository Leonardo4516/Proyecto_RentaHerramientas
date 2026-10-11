package com.rentatools.domain.rental;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class DamageReport {
    private final UUID id;
    private final String description;
    private final BigDecimal penaltyFee;
    private final String evidenceUrl;
    private final Instant reportedAt;

    public DamageReport(UUID id, String description, BigDecimal penaltyFee, String evidenceUrl, Instant reportedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.description = description;
        this.penaltyFee = penaltyFee != null ? penaltyFee : BigDecimal.ZERO;
        this.evidenceUrl = evidenceUrl;
        this.reportedAt = reportedAt != null ? reportedAt : Instant.now();
    }

    public UUID getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getPenaltyFee() { return penaltyFee; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public Instant getReportedAt() { return reportedAt; }
}
