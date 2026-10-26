package com.rentatools.application.tool.dto;

import java.math.BigDecimal;
public record NearbyToolResponse(
        Long id,
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
        String coverImageUrl,
        Long providerId,
        String providerName
) {
    public NearbyToolResponse(
            Long id,
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
    ) {
        this(id, code, name, brand, model, dailyPrice, depositAmount, status, latitude, longitude, distanceKm, coverImageUrl, null, null);
    }
}
