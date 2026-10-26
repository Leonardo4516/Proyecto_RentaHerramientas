package com.rentatools.application.billing;

import com.rentatools.application.billing.dto.InvoiceResponse;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.common.exception.BusinessException;
import com.rentatools.domain.common.exception.ResourceNotFoundException;
import com.rentatools.domain.notification.port.NotificationPort;

public class CancelInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final NotificationPort notificationPort;

    public CancelInvoiceUseCase(InvoiceRepository invoiceRepository, NotificationPort notificationPort) {
        this.invoiceRepository = invoiceRepository;
        this.notificationPort = notificationPort;
    }

    public InvoiceResponse execute(Long invoiceId, String reason) {
        if (invoiceId == null) {
            throw new BusinessException("El ID de la factura es obligatorio");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + invoiceId));

        if (invoice.isCancelled()) {
            throw new BusinessException("La factura ya se encuentra anulada previamente");
        }

        invoice.cancel(reason != null && !reason.isBlank() ? reason : "Nota de crédito / Anulación emitida");
        Invoice saved = invoiceRepository.save(invoice);

        if (notificationPort != null) {
            notificationPort.sendToClient(invoice.getClientId(), "/topic/client/" + invoice.getClientId(),
                    "Tu factura " + invoice.getInvoiceNumber() + " ha sido anulada con nota de crédito. Motivo: " +
                            (reason != null && !reason.isBlank() ? reason : "Solicitud administrativa"));
        }

        return InvoiceResponse.fromDomain(saved);
    }
}
