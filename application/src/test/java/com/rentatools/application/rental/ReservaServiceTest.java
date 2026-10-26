package com.rentatools.application.rental;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.service.ReservaService;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.exception.HerramientaNoDisponibleException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ToolRepository toolRepository;

    private ReservaService reservaService;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(rentalRepository, toolRepository);
    }

    private RentalBooking createSampleBooking(Long id, BookingStatus status) {
        DateRange range = new DateRange(LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        return new RentalBooking(
                id,
                1L,
                10L,
                range,
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(600000),
                status,
                "Av El Dorado # 68C-61, Bogotá",
                "Solicitud para cimentación",
                Instant.now()
        );
    }

    private Tool createSampleTool(Long id, ToolStatus status) {
        return new Tool(
                id,
                1L,
                1L,
                "CAT-320",
                "Retroexcavadora Caterpillar 320D",
                "Caterpillar",
                "320D",
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(500000),
                status,
                0,
                null,
                Instant.now(),
                java.util.List.of(),
                java.util.List.of()
        );
    }

    @Test
    @DisplayName("aprobarReserva debe cambiar estado a APROBADA cuando está PENDIENTE y la herramienta está disponible")
    void shouldApprovePendingReservationSuccessfully() {
        RentalBooking booking = createSampleBooking(100L, BookingStatus.PENDIENTE);
        Tool tool = createSampleTool(10L, ToolStatus.DISPONIBLE);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(toolRepository.findById(10L)).thenReturn(Optional.of(tool));
        when(rentalRepository.hasOtherOverlappingConfirmedBooking(eq(10L), eq(100L), any(), any())).thenReturn(false);
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(i -> i.getArgument(0));

        RentalResponse response = reservaService.aprobarReserva(100L, "proveedor_andina");

        assertNotNull(response);
        assertEquals("APROBADA", response.status());
        verify(rentalRepository).save(booking);
        verify(rentalRepository).recordTimelineEvent(argThat(event ->
                event.rentalId().equals(100L) &&
                "PENDIENTE".equals(event.previousStatus()) &&
                "APROBADA".equals(event.newStatus()) &&
                "proveedor_andina".equals(event.changedBy())
        ));
    }

    @Test
    @DisplayName("aprobarReserva debe lanzar HerramientaNoDisponibleException si existe solapamiento con otra reserva confirmada")
    void shouldThrowHerramientaNoDisponibleExceptionWhenToolHasConflict() {
        RentalBooking booking = createSampleBooking(101L, BookingStatus.PENDIENTE);
        Tool tool = createSampleTool(10L, ToolStatus.DISPONIBLE);

        when(rentalRepository.findById(101L)).thenReturn(Optional.of(booking));
        when(toolRepository.findById(10L)).thenReturn(Optional.of(tool));
        when(rentalRepository.hasOtherOverlappingConfirmedBooking(eq(10L), eq(101L), any(), any())).thenReturn(true);

        assertThrows(HerramientaNoDisponibleException.class, () ->
                reservaService.aprobarReserva(101L, "proveedor_andina")
        );
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("aprobarReserva debe lanzar HerramientaNoDisponibleException si la herramienta está en mantenimiento")
    void shouldThrowHerramientaNoDisponibleExceptionWhenToolInMaintenance() {
        RentalBooking booking = createSampleBooking(102L, BookingStatus.PENDIENTE);
        Tool tool = createSampleTool(10L, ToolStatus.EN_MANTENIMIENTO);

        when(rentalRepository.findById(102L)).thenReturn(Optional.of(booking));
        when(toolRepository.findById(10L)).thenReturn(Optional.of(tool));

        assertThrows(HerramientaNoDisponibleException.class, () ->
                reservaService.aprobarReserva(102L, "proveedor_andina")
        );
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("aprobarReserva debe lanzar BusinessRuleValidationException si la reserva no está en estado PENDIENTE")
    void shouldThrowExceptionWhenApprovingNonPendingReservation() {
        RentalBooking booking = createSampleBooking(103L, BookingStatus.COMPLETADA);
        when(rentalRepository.findById(103L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessRuleValidationException.class, () ->
                reservaService.aprobarReserva(103L, "proveedor_andina")
        );
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechazarReserva debe cambiar estado a RECHAZADA con motivo y registrar timeline")
    void shouldRejectPendingReservationSuccessfully() {
        RentalBooking booking = createSampleBooking(104L, BookingStatus.PENDIENTE);
        when(rentalRepository.findById(104L)).thenReturn(Optional.of(booking));
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(i -> i.getArgument(0));

        RentalResponse response = reservaService.rechazarReserva(104L, "Equipo asignado a obra mayor", "proveedor_andina");

        assertNotNull(response);
        assertEquals("RECHAZADA", response.status());
        verify(rentalRepository).save(booking);
        verify(rentalRepository).recordTimelineEvent(argThat(event ->
                event.rentalId().equals(104L) &&
                "PENDIENTE".equals(event.previousStatus()) &&
                "RECHAZADA".equals(event.newStatus()) &&
                "proveedor_andina".equals(event.changedBy()) &&
                "Equipo asignado a obra mayor".equals(event.reason())
        ));
    }

    @Test
    @DisplayName("rechazarReserva debe lanzar BusinessRuleValidationException si la reserva ya no está PENDIENTE")
    void shouldThrowExceptionWhenRejectingNonPendingReservation() {
        RentalBooking booking = createSampleBooking(105L, BookingStatus.APROBADA);
        when(rentalRepository.findById(105L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessRuleValidationException.class, () ->
                reservaService.rechazarReserva(105L, "Rechazo tardío", "proveedor_andina")
        );
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si la reserva no existe")
    void shouldThrowEntityNotFoundWhenBookingNotFound() {
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                reservaService.aprobarReserva(999L)
        );
        verify(rentalRepository, never()).save(any());
    }
}
