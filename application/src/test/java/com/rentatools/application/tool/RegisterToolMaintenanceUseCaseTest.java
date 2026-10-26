package com.rentatools.application.tool;

import com.rentatools.application.tool.dto.RegisterMaintenanceCommand;
import com.rentatools.application.tool.dto.ToolMaintenanceResponse;
import com.rentatools.application.tool.usecase.RegisterToolMaintenanceUseCase;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.tool.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterToolMaintenanceUseCaseTest {

    @Mock
    private ToolMaintenanceRepository maintenanceRepository;

    @Mock
    private ToolRepository toolRepository;

    private RegisterToolMaintenanceUseCase registerToolMaintenanceUseCase;

    @BeforeEach
    void setUp() {
        registerToolMaintenanceUseCase = new RegisterToolMaintenanceUseCase(maintenanceRepository, toolRepository);
    }

    private Tool buildTool(Long id, ToolStatus status) {
        return new Tool(
                id,
                1L,
                1L,
                "CAT-420F2",
                "Retroexcavadora CAT 420F2",
                "Caterpillar",
                "420F2",
                BigDecimal.valueOf(450000),
                BigDecimal.valueOf(1500000),
                status,
                0,
                new GeoCoordinates(BigDecimal.valueOf(4.65), BigDecimal.valueOf(-74.08)),
                Instant.now(),
                List.of(),
                List.of()
        );
    }

    @Test
    @DisplayName("Debe registrar un mantenimiento preventivo y cambiar estado de herramienta a MANTENIMIENTO")
    void shouldRegisterMaintenanceAndMarkToolInMaintenance() {
        Tool tool = buildTool(1L, ToolStatus.DISPONIBLE);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(tool));

        ToolMaintenanceLog savedLog = new ToolMaintenanceLog(
                100L,
                1L,
                "PREVENTIVO",
                "Cambio de aceite hidráulico y filtros de motor",
                "Ing. Carlos Mendoza",
                BigDecimal.valueOf(350000),
                Instant.now(),
                LocalDate.now().plusMonths(3),
                "Operación exitosa"
        );
        when(maintenanceRepository.save(any(ToolMaintenanceLog.class))).thenReturn(savedLog);

        RegisterMaintenanceCommand command = new RegisterMaintenanceCommand(
                "PREVENTIVO",
                "Cambio de aceite hidráulico y filtros de motor",
                "Ing. Carlos Mendoza",
                BigDecimal.valueOf(350000),
                LocalDate.now().plusMonths(3),
                "Operación exitosa",
                true
        );

        ToolMaintenanceResponse response = registerToolMaintenanceUseCase.execute(1L, command);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("PREVENTIVO", response.maintenanceType());
        assertEquals(BigDecimal.valueOf(350000), response.cost());
        assertEquals(ToolStatus.EN_MANTENIMIENTO, tool.getStatus());
        verify(maintenanceRepository, times(1)).save(any(ToolMaintenanceLog.class));
        verify(toolRepository, times(1)).save(tool);
    }

    @Test
    @DisplayName("Debe lanzar excepción si la herramienta no existe")
    void shouldThrowExceptionWhenToolNotFound() {
        when(toolRepository.findById(999L)).thenReturn(Optional.empty());

        RegisterMaintenanceCommand command = new RegisterMaintenanceCommand(
                "CORRECTIVO",
                "Reparación de cilindro hidráulico",
                "Mecánico Juan",
                BigDecimal.valueOf(500000),
                null,
                null,
                false
        );

        assertThrows(EntityNotFoundException.class, () ->
                registerToolMaintenanceUseCase.execute(999L, command));
    }
}
