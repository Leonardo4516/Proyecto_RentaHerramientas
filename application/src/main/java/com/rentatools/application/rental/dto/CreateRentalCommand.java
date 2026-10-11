package com.rentatools.application.rental.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRentalCommand(
        UUID clientId,
        UUID toolId,
        LocalDate startDate,
        LocalDate endDate,
        String deliveryAddress,
        String notes
) {}
