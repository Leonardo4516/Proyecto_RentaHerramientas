package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.GenerateInvoiceRequest;
import com.rentatools.application.billing.dto.InvoiceItemDto;
import com.rentatools.application.billing.dto.InvoiceResponse;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.notification.port.NotificationPort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class GenerateInvoiceUseCase {
    private static final AtomicLong SEQUENCE = new AtomicLong(1000);

    private final InvoiceRepository invoiceRepository;
    private final NotificationPort notificationPort;

    public GenerateInvoiceUseCase(InvoiceRepository invoiceRepository, NotificationPort notificationPort) {
        this.invoiceRepository = invoiceRepository;
        this.notificationPort = notificationPort;
    }

    public InvoiceResponse execute(GenerateInvoiceRequest request) {
        if (request == null) {
            throw new BusinessException("Datos de factura requeridos");
        }

        if (request.getClientId() == null) {
            throw new BusinessException("El ID del cliente es obligatorio");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("La factura debe tener al menos un ítem");
        }

        String invoiceNumber = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + SEQUENCE.incrementAndGet();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setRentalId(request.getRentalId());
        invoice.setClientId(request.getClientId());
        invoice.setClientName(request.getClientName() != null ? request.getClientName() : "Cliente General");
        invoice.setClientTaxId(request.getClientTaxId() != null ? request.getClientTaxId() : "900.000.000-1");
        invoice.setDepositAmount(request.getDepositAmount());

        long itemId = 1;
        for (InvoiceItemDto itemDto : request.getItems()) {
            InvoiceItem item = new InvoiceItem(
                    itemId++,
                    itemDto.getToolId(),
                    itemDto.getToolName(),
                    itemDto.getDailyRate(),
                    itemDto.getDays()
            );
            invoice.addItem(item);
        }

        invoice.recalculateTotals();
        Invoice saved = invoiceRepository.save(invoice);

        if (notificationPort != null) {
            notificationPort.sendToClient(saved.getClientId(), "/topic/client/" + saved.getClientId(),
                    "Se ha generado la factura " + saved.getInvoiceNumber() + " por un total de $" + saved.getTotalAmount());
        }

        return InvoiceResponse.fromDomain(saved);
    }
}
