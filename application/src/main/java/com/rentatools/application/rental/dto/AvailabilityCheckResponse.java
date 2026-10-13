package com.rentatools.application.rental.dto;

import java.time.LocalDate;
public record AvailabilityCheckResponse(
        Long toolId,
        LocalDate startDate,
        LocalDate endDate,
        boolean isAvailable,
        String message
) {}
