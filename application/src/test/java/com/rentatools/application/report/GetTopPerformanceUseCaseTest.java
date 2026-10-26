package com.rentatools.application.report;

import com.rentatools.application.report.dto.TopPerformanceResponse;
import com.rentatools.domain.report.port.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTopPerformanceUseCaseTest {

    @Mock
    private ReportRepository reportRepository;

    private GetTopPerformanceUseCase getTopPerformanceUseCase;

    @BeforeEach
    void setUp() {
        getTopPerformanceUseCase = new GetTopPerformanceUseCase(reportRepository);
    }

    @Test
    @DisplayName("Debe retornar las métricas consolidadas de rendimiento BI")
    void shouldReturnTopPerformanceMetrics() {
        Map<String, Object> map = new HashMap<>();
        map.put("topToolName", "Retroexcavadora CAT 420F2");
        map.put("topToolRentalsCount", 25L);
        map.put("topToolRevenue", BigDecimal.valueOf(11250000));
        map.put("topClientName", "Constructora Colpatria");
        map.put("topClientSpend", BigDecimal.valueOf(14500000));
        map.put("occupancyRate", 82.5);
        map.put("totalRevenue", BigDecimal.valueOf(35000000));
        map.put("activeRentalsCount", 20L);
        map.put("totalToolsCount", 50L);

        when(reportRepository.getTopPerformance()).thenReturn(map);

        TopPerformanceResponse response = getTopPerformanceUseCase.execute();

        assertNotNull(response);
        assertEquals("Retroexcavadora CAT 420F2", response.getTopToolName());
        assertEquals(25L, response.getTopToolRentalsCount());
        assertEquals(BigDecimal.valueOf(11250000), response.getTopToolRevenue());
        assertEquals("Constructora Colpatria", response.getTopClientName());
        assertEquals(BigDecimal.valueOf(14500000), response.getTopClientSpend());
        assertEquals(82.5, response.getOccupancyRate());
        assertEquals(BigDecimal.valueOf(35000000), response.getTotalRevenue());
        assertEquals(20L, response.getActiveRentalsCount());
        assertEquals(50L, response.getTotalToolsCount());
        verify(reportRepository, times(1)).getTopPerformance();
    }
}
