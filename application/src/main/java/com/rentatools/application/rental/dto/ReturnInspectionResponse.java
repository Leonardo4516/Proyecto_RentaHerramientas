package com.rentatools.application.rental.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReturnInspectionResponse(
        Long id,
        Long rentalId,
        Instant returnDate,
        Long inspectorUserId,
        String equipmentCondition,
        boolean hasDamage,
        BigDecimal originalDeposit,
        BigDecimal totalPenalties,
        BigDecimal depositRefunded,
        String notes,
        List<DamageReportDto> damages
) {}
