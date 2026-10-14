package com.rentatools.infrastructure.persistence.billing;

import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.infrastructure.persistence.repository.RentalJpaRepository;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InvoiceAdapter implements InvoiceRepository {
    private final SpringDataInvoiceRepository invoiceRepo;
    private final RentalJpaRepository rentalRepo;
    private final SpringDataClientRepository clientRepo;
    private final SpringDataUserRepository userRepo;
    private final ToolJpaRepository toolRepo;

    public InvoiceAdapter(SpringDataInvoiceRepository invoiceRepo,
                          RentalJpaRepository rentalRepo,
                          SpringDataClientRepository clientRepo,
                          SpringDataUserRepository userRepo,
                          ToolJpaRepository toolRepo) {
        this.invoiceRepo = invoiceRepo;
        this.rentalRepo = rentalRepo;
        this.clientRepo = clientRepo;
        this.userRepo = userRepo;
        this.toolRepo = toolRepo;
    }

    @Override
    public Invoice save(Invoice invoice) {
        if (invoice == null) return null;
        if (invoice.getId() == null) {
            invoice.setId(UUID.randomUUID());
        }
        InvoiceJpaEntity entity = toEntity(invoice);
        InvoiceJpaEntity saved = invoiceRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return invoiceRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepo.findByInvoiceNumber(invoiceNumber).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByRentalId(UUID rentalId) {
        return invoiceRepo.findByRentalId(rentalId).map(this::toDomain);
    }

    @Override
    public List<Invoice> findByClientId(UUID clientId) {
        return invoiceRepo.findByClientId(clientId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return invoiceRepo.count();
    }

    private InvoiceJpaEntity toEntity(Invoice inv) {
        if (inv == null) return null;
        InvoiceJpaEntity e = new InvoiceJpaEntity();
        e.setId(inv.getId() != null ? inv.getId() : UUID.randomUUID());
        e.setInvoiceNumber(inv.getInvoiceNumber());
        e.setRentalId(inv.getRentalId());
        e.setPaymentId(inv.getPaymentId());
        e.setIssuedAt(inv.getIssuedAt() != null ? inv.getIssuedAt() : Instant.now());
        e.setSubtotal(inv.getSubtotal());
        e.setTax(inv.getTax());
        e.setDeposit(inv.getDeposit());
        e.setTotal(inv.getTotal());
        e.setPdfStoragePath(inv.getPdfStoragePath() != null ? inv.getPdfStoragePath() : "/invoices/" + inv.getInvoiceNumber() + ".pdf");
        return e;
    }

    private Invoice toDomain(InvoiceJpaEntity e) {
        if (e == null) return null;
        Invoice inv = new Invoice();
        inv.setId(e.getId());
        inv.setInvoiceNumber(e.getInvoiceNumber());
        inv.setRentalId(e.getRentalId());
        inv.setPaymentId(e.getPaymentId());
        inv.setIssuedAt(e.getIssuedAt());
        inv.setSubtotal(e.getSubtotal());
        inv.setTax(e.getTax());
        inv.setDeposit(e.getDeposit());
        inv.setTotal(e.getTotal());
        inv.setPdfStoragePath(e.getPdfStoragePath());
        inv.setPaid(e.isPaid());

        // Enriquecer datos a partir de la renta y la herramienta asociada
        if (e.getRentalId() != null) {
            rentalRepo.findById(e.getRentalId()).ifPresent(rental -> {
                inv.setClientId(rental.getClientId());

                // Obtener datos del cliente y usuario
                if (rental.getClientId() != null) {
                    clientRepo.findById(rental.getClientId()).ifPresent(client -> {
                        inv.setClientTaxId(client.getDocumentNumber());
                        if (client.getUserId() != null) {
                            userRepo.findById(client.getUserId()).ifPresent(u -> inv.setClientName(u.getFullName()));
                        }
                    });
                }

                // Generar ítem de factura a partir de la herramienta
                if (rental.getToolId() != null) {
                    String toolName = toolRepo.findById(rental.getToolId())
                            .map(t -> t.getName())
                            .orElse("Herramienta #" + rental.getToolId());

                    InvoiceItem item = new InvoiceItem(
                            UUID.randomUUID(),
                            rental.getToolId(),
                            toolName,
                            rental.getDailyRate(),
                            rental.getTotalDays()
                    );
                    inv.addItem(item);
                }
            });
        }

        return inv;
    }
}
