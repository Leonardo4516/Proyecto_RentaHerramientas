package com.rentatools.application.tool.dto;

import java.math.BigDecimal;

public record UpdateToolCommand(
        String name,
        String brand,
        String model,
        BigDecimal dailyPrice,
        BigDecimal depositAmount
) {}
