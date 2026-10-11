package com.rentatools.domain.tool;

import com.rentatools.domain.common.BusinessRuleValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Tool {
    private final UUID id;
    private final UUID providerId;
    private final UUID categoryId;
    private final String code;
    private String name;
    private String brand;
    private String model;
    private BigDecimal dailyPrice;
    private BigDecimal depositAmount;
    private ToolStatus status;
    private int accumulatedRentalDays;
    private GeoCoordinates coordinates;
    private final Instant createdAt;
    private final List<ToolImage> images;
    private final List<ToolSpecification> specifications;

    public Tool(UUID id, UUID providerId, UUID categoryId, String code, String name, String brand,
                String model, BigDecimal dailyPrice, BigDecimal depositAmount, ToolStatus status,
                int accumulatedRentalDays, GeoCoordinates coordinates, Instant createdAt,
                List<ToolImage> images, List<ToolSpecification> specifications) {
        if (dailyPrice == null || dailyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleValidationException("La tarifa diaria debe ser mayor a 0.");
        }
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleValidationException("El depósito de garantía no puede ser negativo.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.providerId = providerId;
        this.categoryId = categoryId;
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.dailyPrice = dailyPrice;
        this.depositAmount = depositAmount;
        this.status = status != null ? status : ToolStatus.DISPONIBLE;
        this.accumulatedRentalDays = Math.max(0, accumulatedRentalDays);
        this.coordinates = coordinates;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.specifications = specifications != null ? new ArrayList<>(specifications) : new ArrayList<>();
    }

    public void markAsRented() {
        if (this.status != ToolStatus.DISPONIBLE) {
            throw new BusinessRuleValidationException("La herramienta no está disponible para renta. Estado actual: " + this.status);
        }
        this.status = ToolStatus.ALQUILADO;
    }

    public void markAsAvailable() {
        this.status = ToolStatus.DISPONIBLE;
    }

    public void sendToMaintenance() {
        this.status = ToolStatus.EN_MANTENIMIENTO;
    }

    public void addAccumulatedDays(int days) {
        if (days > 0) {
            this.accumulatedRentalDays += days;
        }
    }

    public UUID getId() { return id; }
    public UUID getProviderId() { return providerId; }
    public UUID getCategoryId() { return categoryId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public BigDecimal getDailyPrice() { return dailyPrice; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public ToolStatus getStatus() { return status; }
    public int getAccumulatedRentalDays() { return accumulatedRentalDays; }
    public GeoCoordinates getCoordinates() { return coordinates; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ToolImage> getImages() { return Collections.unmodifiableList(images); }
    public List<ToolSpecification> getSpecifications() { return Collections.unmodifiableList(specifications); }
}
