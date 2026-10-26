package com.rentatools.application.rental;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.usecase.ExtendRentalBookingUseCase;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.tool.ToolRepository;
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
class ExtendRentalBookingUseCaseTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ToolRepository toolRepository;

    private ExtendRentalBookingUseCase extendRentalBookingUseCase;

    @BeforeEach
    void setUp() {
        extendRentalBookingUseCase = new ExtendRentalBookingUseCase(rentalRepository, toolRepository);
    }

    private RentalBooking createSampleBooking(Long id, BookingStatus status, LocalDate start, LocalDate end) {
        DateRange range = new DateRange(start, end);
        return new RentalBooking(
                id,
                10L,
                20L,
                range,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(100000).multiply(BigDecimal.valueOf(range.totalDays())),
                status,
                "Calle 100 # 15-20, Bogotá",
                "Notas iniciales",
                Instant.now()
        );
    }

    @Test
    @DisplayName("Debe extender el alquiler exitosamente cuando no hay solapamiento")
    void shouldExtendRentalSuccessfullyWhenNoOverlap() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(4);
        LocalDate newEnd = LocalDate.now().plusDays(7); // 3 días adicionales

        RentalBooking booking = createSampleBooking(1L, BookingStatus.CONFIRMADA, start, end);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rentalRepository.hasOverlappingBookings(20L, end.plusDays(1), newEnd)).thenReturn(false);
        when(rentalRepository.save(any(RentalBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = extendRentalBookingUseCase.execute(1L, newEnd, "Atraso en excavación", "cliente@constructora.com");

        assertNotNull(response);
        assertEquals(newEnd, response.endDate());
        verify(rentalRepository).save(any(RentalBooking.class));
        verify(rentalRepository).recordTimelineEvent(any());
    }

    @Test
    @DisplayName("Debe rechazar la extensión si la herramienta tiene solapamiento con otra reserva en las nuevas fechas")
    void shouldRejectExtensionWhenOverlapExists() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(4);
        LocalDate newEnd = LocalDate.now().plusDays(7);

        RentalBooking booking = createSampleBooking(1L, BookingStatus.CONFIRMADA, start, end);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rentalRepository.hasOverlappingBookings(20L, end.plusDays(1), newEnd)).thenReturn(true);

        BusinessRuleValidationException ex = assertThrows(BusinessRuleValidationException.class, () ->
                extendRentalBookingUseCase.execute(1L, newEnd, "Extensión", "cliente")
        );

        assertTrue(ex.getMessage().contains("No es posible extender"));
        verify(rentalRepository, never()).save(any());
    }
}
