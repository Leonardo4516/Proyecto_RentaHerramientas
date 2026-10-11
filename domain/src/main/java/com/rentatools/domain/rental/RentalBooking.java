package com.rentatools.domain.rental;

import com.rentatools.domain.common.BusinessRuleValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RentalBooking {
    private final UUID id;
    private final UUID clientId;
    private final UUID toolId;
    private final DateRange dateRange;
    private final BigDecimal dailyRate;
    private final BigDecimal depositAmount;
    private final BigDecimal subtotal;
    private BookingStatus status;
    private final String deliveryAddress;
    private String notes;
    private final Instant createdAt;
    private ReturnInspection inspection;

    public RentalBooking(UUID id, UUID clientId, UUID toolId, DateRange dateRange, BigDecimal dailyRate,
                         BigDecimal depositAmount, BigDecimal subtotal, BookingStatus status,
                         String deliveryAddress, String notes, Instant createdAt) {
        if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleValidationException("La tarifa de alquiler debe ser positiva.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.clientId = clientId;
        this.toolId = toolId;
        this.dateRange = dateRange;
        this.dailyRate = dailyRate;
        this.depositAmount = depositAmount != null ? depositAmount : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal : dailyRate.multiply(BigDecimal.valueOf(dateRange.totalDays()));
        this.status = status != null ? status : BookingStatus.PENDIENTE;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static RentalBooking create(UUID clientId, UUID toolId, DateRange dateRange, BigDecimal dailyRate,
                                       BigDecimal depositAmount, String deliveryAddress, String notes) {
        BigDecimal subtotal = dailyRate.multiply(BigDecimal.valueOf(dateRange.totalDays()));
        return new RentalBooking(null, clientId, toolId, dateRange, dailyRate, depositAmount, subtotal,
                BookingStatus.PENDIENTE, deliveryAddress, notes, Instant.now());
    }

    public void confirm() {
        if (this.status != BookingStatus.PENDIENTE) {
            throw new BusinessRuleValidationException("Solo se pueden confirmar reservas en estado PENDIENTE.");
        }
        this.status = BookingStatus.CONFIRMADA;
    }

    public void reject(String reason) {
        if (this.status != BookingStatus.PENDIENTE) {
            throw new BusinessRuleValidationException("Solo se pueden rechazar reservas en estado PENDIENTE.");
        }
        this.status = BookingStatus.RECHAZADA;
        this.notes = (this.notes != null ? this.notes + " | " : "") + "Rechazo: " + reason;
    }

    public void startRental() {
        if (this.status != BookingStatus.CONFIRMADA) {
            throw new BusinessRuleValidationException("Solo se puede iniciar un alquiler confirmado.");
        }
        this.status = BookingStatus.EN_CURSO;
    }

    public void completeReturn(ReturnInspection inspection) {
        if (this.status != BookingStatus.EN_CURSO) {
            throw new BusinessRuleValidationException("Solo se pueden devolver alquileres EN_CURSO.");
        }
        this.status = BookingStatus.COMPLETADA;
        this.inspection = inspection;
    }

    public void cancel() {
        if (this.status != BookingStatus.PENDIENTE && this.status != BookingStatus.CONFIRMADA) {
            throw new BusinessRuleValidationException("No se puede cancelar un alquiler que ya está en curso o finalizado.");
        }
        this.status = BookingStatus.CANCELADA;
    }

    public UUID getId() { return id; }
    public UUID getClientId() { return clientId; }
    public UUID getToolId() { return toolId; }
    public DateRange getDateRange() { return dateRange; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BookingStatus getStatus() { return status; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public ReturnInspection getInspection() { return inspection; }
}
