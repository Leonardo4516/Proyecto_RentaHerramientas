package com.rentatools.application.rental.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityCheckResponse(
        UUID toolId,
        LocalDate startDate,
        LocalDate endDate,
        boolean isAvailable,
        String message
) {}
