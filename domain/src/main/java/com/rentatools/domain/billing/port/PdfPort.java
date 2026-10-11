package com.rentatools.domain.billing.port;

import com.rentatools.domain.billing.Invoice;

public interface PdfPort {
    byte[] generateInvoicePdf(Invoice invoice);
}
