package com.rentatools.application.tool;

import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.application.tool.usecase.SearchToolsUseCase;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchToolsUseCaseTest {

    @Mock
    private ToolRepository toolRepository;

    private SearchToolsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchToolsUseCase(toolRepository);
    }

    private Tool buildTool(Long id, String code, String name, String brand, String model, BigDecimal price, Long categoryId, ToolStatus status) {
        return new Tool(
                id,
                1L,
                categoryId,
                code,
                name,
                brand,
                model,
                price,
                price.multiply(BigDecimal.valueOf(2)),
                status,
                0,
                new GeoCoordinates(BigDecimal.valueOf(4.65), BigDecimal.valueOf(-74.08)),
                Instant.now(),
                List.of(),
                List.of()
        );
    }

    @Test
    @DisplayName("Debe buscar herramientas por texto coincidente en nombre o marca")
    void shouldFilterByTextQuery() {
        Tool t1 = buildTool(1L, "CAT-01", "Retroexcavadora Oruga", "Caterpillar", "320D", BigDecimal.valueOf(850000), 1L, ToolStatus.DISPONIBLE);
        Tool t2 = buildTool(2L, "BOS-01", "Martillo Demoledor", "Bosch", "GSH27", BigDecimal.valueOf(95000), 4L, ToolStatus.DISPONIBLE);
        Tool t3 = buildTool(3L, "BOB-01", "Minicargador S570", "Bobcat", "S570", BigDecimal.valueOf(350000), 1L, ToolStatus.DISPONIBLE);

        when(toolRepository.findAll()).thenReturn(List.of(t1, t2, t3));

        List<ToolResponse> results = useCase.execute("caterpillar", null, null, null, null);
        assertEquals(1, results.size());
        assertEquals("Retroexcavadora Oruga", results.get(0).name());

        List<ToolResponse> resultsPartial = useCase.execute("oruga", null, null, null, null);
        assertEquals(1, resultsPartial.size());
        assertEquals("CAT-01", resultsPartial.get(0).code());
    }

    @Test
    @DisplayName("Debe filtrar por rango de precios y categoría")
    void shouldFilterByPriceRangeAndCategory() {
        Tool t1 = buildTool(1L, "CAT-01", "Retroexcavadora", "Caterpillar", "320D", BigDecimal.valueOf(850000), 1L, ToolStatus.DISPONIBLE);
        Tool t2 = buildTool(2L, "BOB-01", "Minicargador", "Bobcat", "S570", BigDecimal.valueOf(350000), 1L, ToolStatus.DISPONIBLE);
        Tool t3 = buildTool(3L, "BOS-01", "Martillo Demoledor", "Bosch", "GSH27", BigDecimal.valueOf(95000), 4L, ToolStatus.DISPONIBLE);

        when(toolRepository.findAll()).thenReturn(List.of(t1, t2, t3));

        // Precio entre 100.000 y 400.000 y categoria 1
        List<ToolResponse> results = useCase.execute(null, 1L, BigDecimal.valueOf(100000), BigDecimal.valueOf(400000), null);
        assertEquals(1, results.size());
        assertEquals("Minicargador", results.get(0).name());
    }
}
