package com.rentatools.application.rental.dto;

import java.math.BigDecimal;

public record DamageItemRequest(
        String description,
        BigDecimal penaltyFee,
        String evidenceUrl
) {
    public DamageItemRequest {
        if (penaltyFee == null) {
            penaltyFee = BigDecimal.ZERO;
        }
    }
}
