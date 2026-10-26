package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.PaymentResponse;
import com.rentatools.application.billing.dto.RefundDepositRequest;
import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.PaymentStatus;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.notification.port.NotificationPort;
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
class RefundDepositUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationPort notificationPort;

    private RefundDepositUseCase refundDepositUseCase;

    @BeforeEach
    void setUp() {
        refundDepositUseCase = new RefundDepositUseCase(paymentRepository, rentalRepository, notificationPort);
    }

    private RentalBooking createSampleRental(Long id, BigDecimal deposit) {
        DateRange range = new DateRange(LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        return new RentalBooking(
                id, 10L, 20L, range,
                BigDecimal.valueOf(100000), deposit, BigDecimal.valueOf(400000),
                BookingStatus.COMPLETADA, "Obra", "Notas", Instant.now()
        );
    }

    @Test
    @DisplayName("Debe procesar el reembolso de depósito explícito con estado REFUNDED")
    void shouldProcessDepositRefundSuccessfully() {
        RentalBooking rental = createSampleRental(1L, BigDecimal.valueOf(250000));
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(99L);
            return p;
        });

        RefundDepositRequest request = new RefundDepositRequest(1L, BigDecimal.valueOf(250000), "BANK_TRANSFER", "Reembolso por transferencia");
        PaymentResponse response = refundDepositUseCase.execute(request);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(BigDecimal.valueOf(250000), response.getAmount());
        assertEquals("REFUNDED", response.getStatus());
        assertTrue(response.getTransactionReference().startsWith("REFUND-1-"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Debe calcular el monto a reembolsar a partir de la inspección técnica si no viene en el request")
    void shouldDeriveRefundAmountFromInspectionWhenRequestAmountIsNull() {
        RentalBooking rental = createSampleRental(1L, BigDecimal.valueOf(300000));
        com.rentatools.domain.rental.ReturnInspection inspection = new com.rentatools.domain.rental.ReturnInspection(
                10L, Instant.now(), 1L, "EXCELENTE", false, BigDecimal.valueOf(280000), "Devolución", java.util.List.of()
        );

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.findInspectionByRentalId(1L)).thenReturn(Optional.of(inspection));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        RefundDepositRequest request = new RefundDepositRequest(1L, null, "BANK_TRANSFER", null);
        PaymentResponse response = refundDepositUseCase.execute(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(280000), response.getAmount());
        assertEquals("REFUNDED", response.getStatus());
        verify(rentalRepository).findInspectionByRentalId(1L);
    }

    @Test
    @DisplayName("Debe fallar si el monto liquidado es menor o igual a cero")
    void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
        RentalBooking rental = createSampleRental(2L, BigDecimal.ZERO);
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(rental));
        when(rentalRepository.findInspectionByRentalId(2L)).thenReturn(Optional.empty());

        RefundDepositRequest request = new RefundDepositRequest(2L, null, "BANK_TRANSFER", null);

        BusinessException ex = assertThrows(BusinessException.class, () -> refundDepositUseCase.execute(request));
        assertTrue(ex.getMessage().contains("mayor a 0"));
    }
}
