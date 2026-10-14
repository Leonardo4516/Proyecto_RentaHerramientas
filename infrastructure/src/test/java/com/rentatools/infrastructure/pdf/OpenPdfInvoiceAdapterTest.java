package com.rentatools.infrastructure.pdf;

import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfInvoiceAdapterTest {

    @Test
    @DisplayName("Debe generar un binario de PDF válido que comience con cabecera %PDF")
    void shouldGenerateValidPdfBytes() {
        OpenPdfInvoiceAdapter adapter = new OpenPdfInvoiceAdapter();

        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setInvoiceNumber("INV-202610-001");
        invoice.setRentalId(UUID.randomUUID());
        invoice.setClientId(UUID.randomUUID());
        invoice.setClientName("Empresa Constructora Test");
        invoice.setClientTaxId("900.123.456-1");
        invoice.setDepositAmount(new BigDecimal("200000"));

        InvoiceItem item = new InvoiceItem(UUID.randomUUID(), UUID.randomUUID(), "Andamio Multidireccional", new BigDecimal("50000"), 4);
        invoice.addItem(item);

        byte[] pdfBytes = adapter.generateInvoicePdf(invoice);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        // Verificar firma mágica de archivo PDF (%PDF)
        String header = new String(pdfBytes, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }
}
