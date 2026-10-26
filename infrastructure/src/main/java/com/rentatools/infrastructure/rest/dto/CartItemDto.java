package com.rentatools.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CartItemDto {

    public record Request(
            Long toolId,
            Integer days,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record Response(
            Long id,
            Long userId,
            Long toolId,
            String name,
            String brand,
            String model,
            String code,
            String imageUrl,
            String categoryName,
            String providerName,
            BigDecimal dailyRate,
            BigDecimal depositAmount,
            Integer days,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal subtotal,
            BigDecimal iva,
            BigDecimal deposit,
            BigDecimal total
    ) {}
}
