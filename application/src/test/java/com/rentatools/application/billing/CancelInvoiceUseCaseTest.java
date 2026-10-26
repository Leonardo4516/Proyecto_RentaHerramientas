package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.InvoiceResponse;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.notification.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private NotificationPort notificationPort;

    private CancelInvoiceUseCase cancelInvoiceUseCase;

    @BeforeEach
    void setUp() {
        cancelInvoiceUseCase = new CancelInvoiceUseCase(invoiceRepository, notificationPort);
    }

    private Invoice createSampleInvoice(Long id) {
        Invoice invoice = new Invoice(id, "INV-2026-0001", 10L, 20L, "Constructora Bolívar", "900123456-1", BigDecimal.valueOf(300000));
        invoice.setPaid(true);
        return invoice;
    }

    @Test
    @DisplayName("Debe anular la factura y registrar el motivo de anulación")
    void shouldCancelInvoiceSuccessfully() {
        Invoice invoice = createSampleInvoice(1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        InvoiceResponse response = cancelInvoiceUseCase.execute(1L, "Cancelación de común acuerdo con cliente");

        assertNotNull(response);
        assertFalse(response.isPaid());
        assertTrue(response.isCancelled());
        assertEquals("CANCELADA", response.getStatus());
        assertTrue(response.getPdfStoragePath().contains("Cancelación de común acuerdo"));
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Debe fallar al intentar anular una factura previamente cancelada")
    void shouldFailWhenInvoiceAlreadyCancelled() {
        Invoice invoice = createSampleInvoice(2L);
        invoice.cancel("Cancelada anteriormente");
        when(invoiceRepository.findById(2L)).thenReturn(Optional.of(invoice));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                cancelInvoiceUseCase.execute(2L, "Segunda anulación")
        );

        assertTrue(ex.getMessage().contains("ya se encuentra anulada"));
        verify(invoiceRepository, never()).save(any());
    }
}
