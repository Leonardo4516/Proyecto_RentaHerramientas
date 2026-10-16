package com.rentatools.application.tool.dto;

import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolImage;
import com.rentatools.domain.tool.ToolSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record ToolResponse(
        Long id,
        Long providerId,
        Long categoryId,
        String code,
        String name,
        String brand,
        String model,
        BigDecimal dailyPrice,
        BigDecimal depositAmount,
        String status,
        int accumulatedRentalDays,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt,
        List<String> images,
        List<SpecDto> specifications,
        String providerName
) {
    public ToolResponse(
            Long id,
            Long providerId,
            Long categoryId,
            String code,
            String name,
            String brand,
            String model,
            BigDecimal dailyPrice,
            BigDecimal depositAmount,
            String status,
            int accumulatedRentalDays,
            BigDecimal latitude,
            BigDecimal longitude,
            Instant createdAt,
            List<String> images,
            List<SpecDto> specifications
    ) {
        this(id, providerId, categoryId, code, name, brand, model, dailyPrice, depositAmount, status, accumulatedRentalDays, latitude, longitude, createdAt, images, specifications, null);
    }

    public record SpecDto(String key, String value) {}

    public static ToolResponse fromDomain(Tool tool) {
        List<String> imgUrls = tool.getImages().stream().map(ToolImage::getImageUrl).toList();
        List<SpecDto> specs = tool.getSpecifications().stream().map(s -> new SpecDto(s.getSpecName(), s.getSpecValue())).toList();
        return new ToolResponse(
                tool.getId(),
                tool.getProviderId(),
                tool.getCategoryId(),
                tool.getCode(),
                tool.getName(),
                tool.getBrand(),
                tool.getModel(),
                tool.getDailyPrice(),
                tool.getDepositAmount(),
                tool.getStatus().name(),
                tool.getAccumulatedRentalDays(),
                tool.getCoordinates() != null ? tool.getCoordinates().latitude() : null,
                tool.getCoordinates() != null ? tool.getCoordinates().longitude() : null,
                tool.getCreatedAt(),
                imgUrls,
                specs
        );
    }

    public BigDecimal getDailyRate() {
        return dailyPrice;
    }

    public String getImageUrl() {
        return images != null && !images.isEmpty() ? images.get(0) : "/assets/img/machinery/placeholder-tool.svg";
    }

    public String getCategory() {
        return brand != null && !brand.isBlank() ? brand : "Maquinaria Pesada";
    }

    public String getDescription() {
        return brand + " " + model + " - Código de Inventario: " + code;
    }

    public String getLocationName() {
        return "Bogotá D.C.";
    }

    public String getProviderName() {
        return providerName != null && !providerName.isBlank() ? providerName : "Proveedor Verificado";
    }
}
