package com.rentatools.application.tool.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record NearbyToolResponse(
        UUID id,
        String code,
        String name,
        String brand,
        String model,
        BigDecimal dailyPrice,
        BigDecimal depositAmount,
        String status,
        BigDecimal latitude,
        BigDecimal longitude,
        double distanceKm,
        String coverImageUrl
) {}
