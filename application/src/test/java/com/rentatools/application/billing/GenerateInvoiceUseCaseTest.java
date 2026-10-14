package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.GenerateInvoiceRequest;
import com.rentatools.application.billing.dto.InvoiceItemDto;
import com.rentatools.application.billing.dto.InvoiceResponse;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.notification.port.NotificationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private NotificationPort notificationPort;

    @Test
    @DisplayName("Debe generar factura con cálculos de IVA y depósito correctos")
    void shouldGenerateInvoiceProperly() {
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(returnsFirstArg());

        GenerateInvoiceUseCase useCase = new GenerateInvoiceUseCase(invoiceRepository, notificationPort);

        java.util.UUID rentalId = java.util.UUID.randomUUID();
        java.util.UUID clientId = java.util.UUID.randomUUID();
        java.util.UUID toolId = java.util.UUID.randomUUID();

        GenerateInvoiceRequest req = new GenerateInvoiceRequest();
        req.setRentalId(rentalId);
        req.setClientId(clientId);
        req.setClientName("Constructora S.A.");
        req.setClientTaxId("800.123.456-7");
        req.setDepositAmount(new BigDecimal("1000000.00"));

        InvoiceItemDto item = new InvoiceItemDto(toolId, "Retroexcavadora", new BigDecimal("500000.00"), 2);
        req.setItems(Collections.singletonList(item));

        InvoiceResponse response = useCase.execute(req);

        assertNotNull(response);
        assertTrue(response.getInvoiceNumber().startsWith("INV-"));
        // Subtotal: 2 * 500.000 = 1.000.000
        assertEquals(0, new BigDecimal("1000000.00").compareTo(response.getSubtotal()));
        // IVA 19%: 190.000
        assertEquals(0, new BigDecimal("190000.00").compareTo(response.getTaxAmount()));
        // Depósito: 1.000.000
        assertEquals(0, new BigDecimal("1000000.00").compareTo(response.getDepositAmount()));
        // Total: 1.000.000 + 190.000 + 1.000.000 = 2.190.000
        assertEquals(0, new BigDecimal("2190000.00").compareTo(response.getTotalAmount()));
    }
}
