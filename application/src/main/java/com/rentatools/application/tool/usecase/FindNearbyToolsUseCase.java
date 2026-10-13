package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.NearbyToolResponse;
import com.rentatools.domain.tool.GeoCoordinates;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolImage;
import com.rentatools.domain.tool.ToolRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class FindNearbyToolsUseCase {
    private final ToolRepository toolRepository;

    public FindNearbyToolsUseCase(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public List<NearbyToolResponse> execute(BigDecimal clientLat, BigDecimal clientLng, double radiusKm) {
        GeoCoordinates clientPos = new GeoCoordinates(clientLat, clientLng);
        List<Tool> allTools = toolRepository.findAll();

        return allTools.stream()
                .filter(t -> t.getCoordinates() != null)
                .map(t -> {
                    double dist = clientPos.distanceToKm(t.getCoordinates());
                    String cover = t.getImages().stream()
                            .filter(ToolImage::isCover)
                            .map(ToolImage::getImageUrl)
                            .findFirst()
                            .orElse(t.getImages().isEmpty() ? "/assets/img/machinery/placeholder-tool.svg" : t.getImages().get(0).getImageUrl());
                    if (cover == null || cover.isBlank()) {
                        cover = "/assets/img/machinery/placeholder-tool.svg";
                    }
                    return new NearbyToolResponse(
                            t.getId(),
                            t.getCode(),
                            t.getName(),
                            t.getBrand(),
                            t.getModel(),
                            t.getDailyPrice(),
                            t.getDepositAmount(),
                            t.getStatus().name(),
                            t.getCoordinates().latitude(),
                            t.getCoordinates().longitude(),
                            Math.round(dist * 100.0) / 100.0,
                            cover
                    );
                })
                .filter(dto -> dto.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(NearbyToolResponse::distanceKm))
                .toList();
    }
}
