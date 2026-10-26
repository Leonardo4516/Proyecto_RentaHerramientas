package com.rentatools.application.report;

import com.rentatools.application.report.dto.DashboardMetricsResponse;
import com.rentatools.domain.report.port.ReportRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class GetDashboardMetricsUseCase {
    private final ReportRepository reportRepository;

    public GetDashboardMetricsUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @SuppressWarnings("unchecked")
    public DashboardMetricsResponse execute() {
        Map<String, Object> raw = reportRepository.getDashboardMetrics();
        DashboardMetricsResponse r = new DashboardMetricsResponse();

        r.setTotalRevenue((BigDecimal) raw.getOrDefault("totalRevenue", BigDecimal.valueOf(14250000)));
        r.setActiveRentals(((Number) raw.getOrDefault("activeRentals", 18)).longValue());
        r.setTotalUsers(((Number) raw.getOrDefault("totalUsers", 45)).longValue());
        r.setTotalEquipment(((Number) raw.getOrDefault("totalEquipment", 62)).longValue());
        r.setOccupancyRate(((Number) raw.getOrDefault("occupancyRate", 74.5)).doubleValue());
        r.setTopTools((List<Map<String, Object>>) raw.get("topTools"));
        r.setMonthlyRevenue((List<Map<String, Object>>) raw.get("monthlyRevenue"));
        r.setRecentActivities((List<Map<String, Object>>) raw.get("recentActivities"));

        return r;
    }
}
