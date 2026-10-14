package com.rentatools.infrastructure.adapters.in.rest;

import com.rentatools.application.billing.DownloadPdfUseCase;
import com.rentatools.application.billing.GenerateInvoiceUseCase;
import com.rentatools.application.billing.dto.GenerateInvoiceRequest;
import com.rentatools.application.billing.dto.InvoiceResponse;
import com.rentatools.domain.billing.port.InvoiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Facturación", description = "Generación de facturas electrónicas y descarga oficial en PDF")
public class InvoiceController {

    private final GenerateInvoiceUseCase generateInvoiceUseCase;
    private final DownloadPdfUseCase downloadPdfUseCase;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(GenerateInvoiceUseCase generateInvoiceUseCase, DownloadPdfUseCase downloadPdfUseCase, InvoiceRepository invoiceRepository) {
        this.generateInvoiceUseCase = generateInvoiceUseCase;
        this.downloadPdfUseCase = downloadPdfUseCase;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping
    @Operation(summary = "Generar factura", description = "Crea una nueva factura con desglose fiscal (IVA 19%, depósito y subtotal)")
    public ResponseEntity<InvoiceResponse> generateInvoice(@RequestBody GenerateInvoiceRequest request) {
        InvoiceResponse response = generateInvoiceUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar factura por ID", description = "Obtiene los detalles y estado de una factura")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(InvoiceResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Consultar facturas de un cliente", description = "Obtiene todas las facturas pertenecientes a un cliente")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByClient(@PathVariable Long clientId) {
        List<InvoiceResponse> list = invoiceRepository.findByClientId(clientId).stream()
                .map(InvoiceResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/rental/{rentalId}")
    @Operation(summary = "Consultar factura por ID de reserva", description = "Obtiene la factura asociada a un alquiler específico")
    public ResponseEntity<InvoiceResponse> getInvoiceByRentalId(@PathVariable Long rentalId) {
        return invoiceRepository.findByRentalId(rentalId)
                .map(InvoiceResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar comprobante en PDF", description = "Genera y descarga en tiempo real el comprobante oficial en PDF con OpenPDF")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdfBytes = downloadPdfUseCase.execute(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "factura-rentatools-" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
