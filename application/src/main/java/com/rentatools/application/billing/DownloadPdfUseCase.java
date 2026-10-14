package com.rentatools.application.billing;

import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.billing.port.PdfPort;
import com.rentatools.domain.common.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

public class DownloadPdfUseCase {
    private final InvoiceRepository invoiceRepository;
    private final PdfPort pdfPort;

    public DownloadPdfUseCase(InvoiceRepository invoiceRepository, PdfPort pdfPort) {
        this.invoiceRepository = invoiceRepository;
        this.pdfPort = pdfPort;
    }

    public byte[] execute(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + invoiceId));

        byte[] pdfBytes = pdfPort.generateInvoicePdf(invoice);
        invoice.setPdfGeneratedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        return pdfBytes;
    }
}
