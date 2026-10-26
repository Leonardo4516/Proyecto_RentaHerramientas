package com.rentatools.application.rental;

import com.rentatools.application.rental.dto.DamageItemRequest;
import com.rentatools.application.rental.dto.RegisterReturnInspectionCommand;
import com.rentatools.application.rental.dto.ReturnInspectionResponse;
import com.rentatools.application.rental.usecase.RegisterReturnInspectionUseCase;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.notification.port.NotificationPort;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.ReturnInspection;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterReturnInspectionUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationPort notificationPort;

    private RegisterReturnInspectionUseCase registerReturnInspectionUseCase;

    @BeforeEach
    void setUp() {
        registerReturnInspectionUseCase = new RegisterReturnInspectionUseCase(
                rentalRepository, toolRepository, paymentRepository, notificationPort
        );
    }

    private RentalBooking createSampleRental(Long id, BigDecimal deposit, BookingStatus status) {
        DateRange range = new DateRange(LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        return new RentalBooking(
                id, 10L, 20L, range,
                BigDecimal.valueOf(100000), deposit, BigDecimal.valueOf(400000),
                status, "Obra Central", "Notas", Instant.now()
        );
    }

    private Tool createSampleTool(Long id) {
        return new Tool(
                id, 1L, 2L, "TL-001", "Martillo Demoledor", "Bosch", "GSH 11",
                BigDecimal.valueOf(100000), BigDecimal.valueOf(500000),
                ToolStatus.ALQUILADO, 10, null, Instant.now(), List.of(), List.of()
        );
    }

    @Test
    @DisplayName("Debe liquidar la devolución sin daños con reembolso total del depósito y marcar herramienta DISPONIBLE")
    void shouldCompleteReturnWithFullDepositRefundWhenNoDamages() {
        RentalBooking rental = createSampleRental(1L, BigDecimal.valueOf(500000), BookingStatus.EN_CURSO);
        Tool tool = createSampleTool(20L);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(toolRepository.findById(20L)).thenReturn(Optional.of(tool));
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(i -> i.getArgument(0));
        when(rentalRepository.saveInspection(eq(1L), any(ReturnInspection.class)))
                .thenAnswer(i -> i.getArgument(1));

        RegisterReturnInspectionCommand command = new RegisterReturnInspectionCommand(
                1L, "EXCELENTE", "Equipo entregado limpio y probado", List.of(), true
        );

        ReturnInspectionResponse response = registerReturnInspectionUseCase.execute(1L, command, "inspector@rentatools.com");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(500000), response.originalDeposit());
        assertEquals(BigDecimal.ZERO, response.totalPenalties());
        assertEquals(BigDecimal.valueOf(500000), response.depositRefunded());
        assertFalse(response.hasDamage());
        assertEquals(BookingStatus.COMPLETADA, rental.getStatus());
        assertEquals(ToolStatus.DISPONIBLE, tool.getStatus());

        verify(rentalRepository).save(any(RentalBooking.class));
        verify(rentalRepository).saveInspection(eq(1L), any(ReturnInspection.class));
        verify(toolRepository).save(tool);
        verify(rentalRepository).recordTimelineEvent(any());
        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("Debe descontar penalización por daños del depósito y calcular el remanente")
    void shouldDeductPenaltiesFromDepositAndRefundRemaining() {
        RentalBooking rental = createSampleRental(2L, BigDecimal.valueOf(500000), BookingStatus.CONFIRMADA);
        Tool tool = createSampleTool(20L);

        when(rentalRepository.findById(2L)).thenReturn(Optional.of(rental));
        when(toolRepository.findById(20L)).thenReturn(Optional.of(tool));
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(i -> i.getArgument(0));
        when(rentalRepository.saveInspection(eq(2L), any(ReturnInspection.class)))
                .thenAnswer(i -> i.getArgument(1));

        DamageItemRequest damage = new DamageItemRequest("Cincel roto por sobreesfuerzo", BigDecimal.valueOf(120000), "https://evidence.jpg");
        RegisterReturnInspectionCommand command = new RegisterReturnInspectionCommand(
                1L, "DAÑOS_LEVES", "Punta desgastada y cincel averiado", List.of(damage), true
        );

        ReturnInspectionResponse response = registerReturnInspectionUseCase.execute(2L, command, "inspector@rentatools.com");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(500000), response.originalDeposit());
        assertEquals(BigDecimal.valueOf(120000), response.totalPenalties());
        assertEquals(BigDecimal.valueOf(380000), response.depositRefunded());
        assertTrue(response.hasDamage());
        assertEquals(1, response.damages().size());
        assertEquals(ToolStatus.DISPONIBLE, tool.getStatus());
    }

    @Test
    @DisplayName("Debe enviar la herramienta a MANTENIMIENTO cuando el reporte es de daños graves")
    void shouldSendToolToMaintenanceWhenSevereDamages() {
        RentalBooking rental = createSampleRental(3L, BigDecimal.valueOf(300000), BookingStatus.EN_CURSO);
        Tool tool = createSampleTool(20L);

        when(rentalRepository.findById(3L)).thenReturn(Optional.of(rental));
        when(toolRepository.findById(20L)).thenReturn(Optional.of(tool));
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(i -> i.getArgument(0));
        when(rentalRepository.saveInspection(eq(3L), any(ReturnInspection.class)))
                .thenAnswer(i -> i.getArgument(1));

        DamageItemRequest damage = new DamageItemRequest("Motor quemado", BigDecimal.valueOf(400000), null);
        RegisterReturnInspectionCommand command = new RegisterReturnInspectionCommand(
                1L, "DAÑOS_GRAVES", "Requiere taller técnico especializado", List.of(damage), true
        );

        ReturnInspectionResponse response = registerReturnInspectionUseCase.execute(3L, command, "inspector@rentatools.com");

        assertNotNull(response);
        // Depósito original: 300,000. Penalidades: 400,000. Depósito reembolsado = max(0, -100,000) = 0
        assertEquals(BigDecimal.ZERO, response.depositRefunded());
        assertEquals(ToolStatus.EN_MANTENIMIENTO, tool.getStatus());
        verify(toolRepository).save(tool);
    }
}
