package com.rentatools.application.tool;

import com.rentatools.application.tool.dto.NearbyToolResponse;
import com.rentatools.application.tool.usecase.FindNearbyToolsUseCase;
import com.rentatools.domain.tool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindNearbyToolsUseCaseTest {

    @Mock
    private ToolRepository toolRepository;

    private FindNearbyToolsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindNearbyToolsUseCase(toolRepository);
    }

    private Tool createTool(Long id, String code, BigDecimal lat, BigDecimal lng, String name) {
        return new Tool(
                id,
                1L,
                1L,
                code,
                name,
                "CAT",
                "Model-X",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(200000),
                ToolStatus.DISPONIBLE,
                0,
                new GeoCoordinates(lat, lng),
                Instant.now(),
                List.of(new ToolImage(1L, "https://example.com/tool.jpg", true)),
                List.of()
        );
    }

    @Test
    @DisplayName("Debe encontrar maquinaria dentro del radio ordenado por cercanía")
    void shouldFindNearbyToolsWithinRadiusSorted() {
        // Obra del cliente en Bogotá (Parque de la 93)
        BigDecimal clientLat = BigDecimal.valueOf(4.6766);
        BigDecimal clientLng = BigDecimal.valueOf(-74.0483);

        // Herramienta A: Chapinero (~3.5 km)
        Tool toolA = createTool(1L, "TOOL-A", BigDecimal.valueOf(4.6486), BigDecimal.valueOf(-74.0628), "Taladro Percutor");
        // Herramienta B: Usaquén (~4.2 km)
        Tool toolB = createTool(2L, "TOOL-B", BigDecimal.valueOf(4.7110), BigDecimal.valueOf(-74.0315), "Compresor de Aire");
        // Herramienta C: Medellín (~240 km)
        Tool toolC = createTool(3L, "TOOL-C", BigDecimal.valueOf(6.2442), BigDecimal.valueOf(-75.5812), "Retroexcavadora");

        when(toolRepository.findWithinBoundingBox(any(), any(), any(), any()))
                .thenReturn(List.of(toolA, toolB));

        List<NearbyToolResponse> result = useCase.execute(clientLat, clientLng, 20.0);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TOOL-A", result.get(0).code());
        assertEquals("TOOL-B", result.get(1).code());
        assertTrue(result.get(0).distanceKm() < result.get(1).distanceKm());
        verify(toolRepository, times(1)).findWithinBoundingBox(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía si las coordenadas del cliente son nulas")
    void shouldReturnEmptyWhenCoordinatesNull() {
        List<NearbyToolResponse> result = useCase.execute(null, null, 15.0);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(toolRepository);
    }
}
