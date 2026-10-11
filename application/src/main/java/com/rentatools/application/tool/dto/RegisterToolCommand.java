package com.rentatools.application.tool.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RegisterToolCommand(
        UUID providerId,
        UUID categoryId,
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
