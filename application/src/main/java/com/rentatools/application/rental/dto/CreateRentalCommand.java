package com.rentatools.application.rental.dto;

import java.time.LocalDate;
public record CreateRentalCommand(
        Long clientId,
        Long toolId,
        LocalDate startDate,
        LocalDate endDate,
        String deliveryAddress,
        String notes
) {}
