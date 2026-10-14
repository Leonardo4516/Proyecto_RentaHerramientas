package com.rentatools.domain.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {

    @Test
    @DisplayName("Debe calcular subtotal, IVA del 19% y total incluyendo depósito de garantía")
    void shouldCalculateTotalsCorrectly() {
        Invoice invoice = new Invoice();
        invoice.setDepositAmount(new BigDecimal("500000.00"));

        // 3 días a $ 100.000 = $ 300.000
        InvoiceItem item1 = new InvoiceItem(UUID.randomUUID(), UUID.randomUUID(), "Taladro Demoledor", new BigDecimal("100000.00"), 3);
        invoice.addItem(item1);

        // Subtotal = 300.000
        assertThat(invoice.getSubtotal()).isEqualByComparingTo("300000.00");
        // IVA 19% = 57.000
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo("57000.00");
        // Total = 300.000 + 57.000 + 500.000 = 857.000
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo("857000.00");
    }
}
