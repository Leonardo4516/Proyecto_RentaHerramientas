package com.rentatools.infrastructure.adapters.in.rest;

import com.rentatools.application.report.GetDashboardMetricsUseCase;
import com.rentatools.application.report.GetProviderStatsUseCase;
import com.rentatools.application.report.dto.DashboardMetricsResponse;
import com.rentatools.application.report.dto.ProviderStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reportes & KPIs", description = "Métricas de negocio, ingresos y estadísticas para Administradores y Proveedores")
public class ReportController {

    private final GetDashboardMetricsUseCase dashboardMetricsUseCase;
    private final GetProviderStatsUseCase providerStatsUseCase;

    public ReportController(GetDashboardMetricsUseCase dashboardMetricsUseCase, GetProviderStatsUseCase providerStatsUseCase) {
        this.dashboardMetricsUseCase = dashboardMetricsUseCase;
        this.providerStatsUseCase = providerStatsUseCase;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Métricas globales del Dashboard", description = "Retorna KPIs de ingresos totales, ocupación, usuarios, herramientas top y actividad reciente")
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics() {
        DashboardMetricsResponse response = dashboardMetricsUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider/{providerId}")
    @Operation(summary = "Estadísticas por Proveedor", description = "Retorna métricas individuales de un proveedor específico")
    public ResponseEntity<ProviderStatsResponse> getProviderStats(@PathVariable Long providerId) {
        ProviderStatsResponse response = providerStatsUseCase.execute(providerId);
        return ResponseEntity.ok(response);
    }
}
