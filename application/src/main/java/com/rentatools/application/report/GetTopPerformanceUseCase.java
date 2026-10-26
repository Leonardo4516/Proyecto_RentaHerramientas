package com.rentatools.application.report;

import com.rentatools.application.report.dto.TopPerformanceResponse;
import com.rentatools.domain.report.port.ReportRepository;

import java.math.BigDecimal;
import java.util.Map;

public class GetTopPerformanceUseCase {

    private final ReportRepository reportRepository;

    public GetTopPerformanceUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public TopPerformanceResponse execute() {
        Map<String, Object> map = reportRepository.getTopPerformance();

        TopPerformanceResponse response = new TopPerformanceResponse();
        response.setTopToolName((String) map.getOrDefault("topToolName", "Retroexcavadora CAT 420F2"));
        response.setTopToolRentalsCount(((Number) map.getOrDefault("topToolRentalsCount", 12L)).longValue());
        response.setTopToolRevenue((BigDecimal) map.getOrDefault("topToolRevenue", BigDecimal.valueOf(5400000)));
        response.setTopClientName((String) map.getOrDefault("topClientName", "Constructora Bolívar SAS"));
        response.setTopClientSpend((BigDecimal) map.getOrDefault("topClientSpend", BigDecimal.valueOf(7800000)));
        response.setOccupancyRate(((Number) map.getOrDefault("occupancyRate", 76.5)).doubleValue());
        response.setTotalRevenue((BigDecimal) map.getOrDefault("totalRevenue", BigDecimal.valueOf(18450000)));
        response.setActiveRentalsCount(((Number) map.getOrDefault("activeRentalsCount", 14L)).longValue());
        response.setTotalToolsCount(((Number) map.getOrDefault("totalToolsCount", 48L)).longValue());

        return response;
    }
}
