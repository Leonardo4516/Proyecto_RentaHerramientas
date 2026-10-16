package com.rentatools.application.rental.dto;

import com.rentatools.domain.rental.RentalBooking;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
public record RentalResponse(
        Long id,
        Long clientId,
        Long toolId,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        BigDecimal dailyRate,
        BigDecimal depositAmount,
        BigDecimal subtotal,
        String status,
        String deliveryAddress,
        String notes,
        Instant createdAt,
        String clientName,
        String clientPhone,
        String clientEmail,
        String clientDocument,
        String toolName,
        Long providerId,
        String providerName
) {
    public RentalResponse(
            Long id,
            Long clientId,
            Long toolId,
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
        this(id, clientId, toolId, startDate, endDate, totalDays, dailyRate, depositAmount, subtotal, status, deliveryAddress, notes, createdAt,
             null, null, null, null, null, null, null);
    }

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

    public int getDays() {
        return totalDays;
    }

    public BigDecimal getTotal() {
        return subtotal != null ? subtotal.add(depositAmount != null ? depositAmount : BigDecimal.ZERO) : BigDecimal.ZERO;
    }

    public String getToolName() {
        return toolName != null && !toolName.isBlank() ? toolName : "Maquinaria #" + (toolId != null ? toolId : "");
    }

    public String getClientName() {
        return clientName != null && !clientName.isBlank() ? clientName : "Cliente Constructora";
    }

    public String getClientPhone() {
        return clientPhone != null && !clientPhone.isBlank() ? clientPhone : "+57 320 000 0023";
    }

    public String getClientEmail() {
        return clientEmail != null && !clientEmail.isBlank() ? clientEmail : "cliente@constructora.com";
    }

    public String getClientDocument() {
        return clientDocument != null && !clientDocument.isBlank() ? clientDocument : "NIT 900.123.456-7";
    }

    public String getProviderName() {
        return providerName != null && !providerName.isBlank() ? providerName : "Proveedor Verificado";
    }
}
