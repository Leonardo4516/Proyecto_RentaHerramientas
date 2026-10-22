package com.rentatools.application.rental.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DamageReportDto(
        Long id,
        String description,
        BigDecimal penaltyFee,
        String evidenceUrl,
        Instant reportedAt
) {}
