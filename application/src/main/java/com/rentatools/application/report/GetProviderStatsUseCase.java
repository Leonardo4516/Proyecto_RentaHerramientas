package com.rentatools.application.report;

import com.rentatools.application.report.dto.ProviderStatsResponse;
import com.rentatools.domain.report.port.ReportRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class GetProviderStatsUseCase {
    private final ReportRepository reportRepository;

    public GetProviderStatsUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @SuppressWarnings("unchecked")
    public ProviderStatsResponse execute(Long providerId) {
        Map<String, Object> raw = reportRepository.getProviderStats(providerId);
        ProviderStatsResponse r = new ProviderStatsResponse();

        r.setProviderId(providerId);
        r.setBusinessName((String) raw.getOrDefault("businessName", "Equipos & Maquinaria SAS"));
        r.setTotalTools(((Number) raw.getOrDefault("totalTools", 14)).longValue());
        r.setActiveRentals(((Number) raw.getOrDefault("activeRentals", 5)).longValue());
        r.setMonthlyEarnings((BigDecimal) raw.getOrDefault("monthlyEarnings", BigDecimal.valueOf(6800000)));
        r.setRating(((Number) raw.getOrDefault("rating", 4.9)).doubleValue());
        r.setRentalHistory((List<Map<String, Object>>) raw.get("rentalHistory"));

        return r;
    }
}
