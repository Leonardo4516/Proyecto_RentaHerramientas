package com.rentatools.application.tool;

import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.application.tool.dto.UpdateToolCommand;
import com.rentatools.application.tool.usecase.UpdateToolUseCase;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateToolUseCaseTest {

    @Mock
    private ToolRepository toolRepository;

    private UpdateToolUseCase updateToolUseCase;

    @BeforeEach
    void setUp() {
        updateToolUseCase = new UpdateToolUseCase(toolRepository);
    }

    private Tool buildTool(Long id, BigDecimal dailyPrice, BigDecimal depositAmount, ToolStatus status) {
        return new Tool(
                id,
                1L,
                1L,
                "CAT-320D",
                "Retroexcavadora",
                "Caterpillar",
                "320D",
                dailyPrice,
                depositAmount,
                status,
                0,
                new GeoCoordinates(BigDecimal.valueOf(4.65), BigDecimal.valueOf(-74.08)),
                Instant.now(),
                List.of(),
                List.of()
        );
    }

    @Test
    @DisplayName("Debe actualizar tarifas y modelo de la herramienta")
    void shouldUpdateToolDetails() {
        Tool tool = buildTool(1L, BigDecimal.valueOf(800000), BigDecimal.valueOf(2000000), ToolStatus.DISPONIBLE);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(tool));
        when(toolRepository.save(any(Tool.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateToolCommand cmd = new UpdateToolCommand(
                "Retroexcavadora Oruga Premium",
                "Caterpillar",
                "320D Series 2",
                BigDecimal.valueOf(950000),
                BigDecimal.valueOf(2500000)
        );

        ToolResponse response = updateToolUseCase.execute(1L, cmd);

        assertNotNull(response);
        assertEquals("Retroexcavadora Oruga Premium", response.name());
        assertEquals(BigDecimal.valueOf(950000), response.dailyPrice());
        assertEquals(BigDecimal.valueOf(2500000), response.depositAmount());
        verify(toolRepository).save(any(Tool.class));
    }

    @Test
    @DisplayName("Debe actualizar el estado operativo a EN_MANTENIMIENTO")
    void shouldUpdateToolStatusToMaintenance() {
        Tool tool = buildTool(1L, BigDecimal.valueOf(800000), BigDecimal.valueOf(2000000), ToolStatus.DISPONIBLE);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(tool));
        when(toolRepository.save(any(Tool.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToolResponse response = updateToolUseCase.updateStatus(1L, ToolStatus.EN_MANTENIMIENTO);

        assertNotNull(response);
        assertEquals("EN_MANTENIMIENTO", response.status());
        verify(toolRepository).save(any(Tool.class));
    }
}
