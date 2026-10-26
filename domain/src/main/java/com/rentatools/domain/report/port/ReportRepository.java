package com.rentatools.domain.report.port;

import java.util.Map;

public interface ReportRepository {
    Map<String, Object> getDashboardMetrics();
    Map<String, Object> getProviderStats(Long providerId);
    default Map<String, Object> getTopPerformance() {
        return java.util.Map.of();
    }
}
