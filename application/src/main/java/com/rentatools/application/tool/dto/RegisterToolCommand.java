package com.rentatools.application.tool.dto;

import java.math.BigDecimal;
import java.util.List;
public record RegisterToolCommand(
        Long providerId,
        Long categoryId,
        String code,
        String name,
        String brand,
        String model,
        BigDecimal dailyPrice,
        BigDecimal depositAmount,
        BigDecimal latitude,
        BigDecimal longitude,
        List<String> imageUrls,
        List<SpecCommand> specifications
) {
    public record SpecCommand(String key, String value) {}
}
