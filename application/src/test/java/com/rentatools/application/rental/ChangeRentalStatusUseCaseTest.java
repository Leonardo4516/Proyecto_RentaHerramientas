package com.rentatools.application.rental;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.usecase.ChangeRentalStatusUseCase;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
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
class ChangeRentalStatusUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    private ChangeRentalStatusUseCase changeRentalStatusUseCase;

    @BeforeEach
    void setUp() {
        changeRentalStatusUseCase = new ChangeRentalStatusUseCase(rentalRepository);
    }

    private RentalBooking createSampleBooking(Long id, BookingStatus initialStatus) {
        DateRange range = new DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        return new RentalBooking(
                id,
                10L,
                20L,
                range,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(400000),
                initialStatus,
                "Cra 68 # 45-12, Bogotá",
                "Notas iniciales",
                Instant.now()
        );
    }

    @Test
    @DisplayName("Debe confirmar una reserva pendiente exitosamente")
    void shouldConfirmPendingRentalSuccessfully() {
        RentalBooking booking = createSampleBooking(1L, BookingStatus.PENDIENTE);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = changeRentalStatusUseCase.execute(1L, BookingStatus.CONFIRMADA, null, "admin_user");

        assertNotNull(response);
        assertEquals("CONFIRMADA", response.status());
        verify(rentalRepository).save(booking);
        verify(rentalRepository).recordTimelineEvent(argThat(event ->
                event.rentalId().equals(1L) &&
                "PENDIENTE".equals(event.previousStatus()) &&
                "CONFIRMADA".equals(event.newStatus()) &&
                "admin_user".equals(event.changedBy())
        ));
    }

    @Test
    @DisplayName("Debe rechazar una reserva pendiente con motivo especificado y registrar auditoría")
    void shouldRejectPendingRentalWithReason() {
        RentalBooking booking = createSampleBooking(2L, BookingStatus.PENDIENTE);
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(booking));
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = changeRentalStatusUseCase.execute(2L, BookingStatus.RECHAZADA, "Maquinaria en calibración técnica", "proveedor_valle");

        assertNotNull(response);
        assertEquals("RECHAZADA", response.status());
        verify(rentalRepository).save(booking);
        verify(rentalRepository).recordTimelineEvent(argThat(event ->
                event.rentalId().equals(2L) &&
                "PENDIENTE".equals(event.previousStatus()) &&
                "RECHAZADA".equals(event.newStatus()) &&
                "proveedor_valle".equals(event.changedBy()) &&
                "Maquinaria en calibración técnica".equals(event.reason())
        ));
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si la reserva no existe")
    void shouldThrowExceptionWhenRentalNotFound() {
        when(rentalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                changeRentalStatusUseCase.execute(999L, BookingStatus.CONFIRMADA, null)
        );
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Examen: Debe aprobar una reserva pendiente cambiando su estado a APROBADA")
    void shouldApprovePendingRentalSuccessfully() {
        RentalBooking booking = createSampleBooking(10L, BookingStatus.PENDIENTE);
        when(rentalRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(rentalRepository.hasOverlappingBookingsExcluding(any(), any(), any(), any())).thenReturn(false);
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = changeRentalStatusUseCase.execute(10L, BookingStatus.APROBADA, "Aprobada por proveedor", "proveedor_valle");

        assertNotNull(response);
        assertEquals("APROBADA", response.status());
        verify(rentalRepository).save(booking);
    }

    @Test
    @DisplayName("Examen: Debe lanzar HerramientaNoDisponibleException al aprobar si hay solapamiento de fechas")
    void shouldThrowHerramientaNoDisponibleExceptionWhenToolHasOverlapOnApproval() {
        RentalBooking booking = createSampleBooking(11L, BookingStatus.PENDIENTE);
        when(rentalRepository.findById(11L)).thenReturn(Optional.of(booking));
        when(rentalRepository.hasOverlappingBookingsExcluding(any(), any(), any(), any())).thenReturn(true);

        assertThrows(com.rentatools.domain.common.exception.HerramientaNoDisponibleException.class, () ->
                changeRentalStatusUseCase.execute(11L, BookingStatus.APROBADA, null, "proveedor")
        );
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Examen: Debe rechazar la aprobación si el estado actual no es PENDIENTE")
    void shouldRejectApprovalWhenNotPending() {
        RentalBooking booking = createSampleBooking(12L, BookingStatus.RECHAZADA);
        when(rentalRepository.findById(12L)).thenReturn(Optional.of(booking));

        assertThrows(com.rentatools.domain.common.BusinessRuleValidationException.class, () ->
                changeRentalStatusUseCase.execute(12L, BookingStatus.APROBADA, null, "proveedor")
        );
        verify(rentalRepository, never()).save(any());
    }
}
