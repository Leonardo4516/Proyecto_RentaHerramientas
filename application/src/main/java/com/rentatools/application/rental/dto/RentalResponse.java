package com.rentatools.application.rental.dto;

import com.rentatools.domain.rental.RentalBooking;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        UUID clientId,
        UUID toolId,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        BigDecimal dailyRate,
        BigDecimal depositAmount,
        BigDecimal subtotal,
        String status,
        String deliveryAddress,
        String notes,
        Instant createdAt
) {
    public static RentalResponse fromDomain(RentalBooking rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getClientId(),
                rental.getToolId(),
                rental.getDateRange().startDate(),
                rental.getDateRange().endDate(),
                rental.getDateRange().totalDays(),
                rental.getDailyRate(),
                rental.getDepositAmount(),
                rental.getSubtotal(),
                rental.getStatus().name(),
                rental.getDeliveryAddress(),
                rental.getNotes(),
                rental.getCreatedAt()
        );
    }
}
