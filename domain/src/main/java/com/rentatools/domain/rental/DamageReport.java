package com.rentatools.domain.rental;

import java.math.BigDecimal;
import java.time.Instant;
public class DamageReport {
    private final Long id;
    private final String description;
    private final BigDecimal penaltyFee;
    private final String evidenceUrl;
    private final Instant reportedAt;

    public DamageReport(Long id, String description, BigDecimal penaltyFee, String evidenceUrl, Instant reportedAt) {
        this.id = id != null ? id : null;
        this.description = description;
        this.penaltyFee = penaltyFee != null ? penaltyFee : BigDecimal.ZERO;
        this.evidenceUrl = evidenceUrl;
        this.reportedAt = reportedAt != null ? reportedAt : Instant.now();
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getPenaltyFee() { return penaltyFee; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public Instant getReportedAt() { return reportedAt; }
}
