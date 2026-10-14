package com.rentatools.infrastructure.persistence.billing;

import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;
import com.rentatools.domain.billing.port.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InvoiceAdapter implements InvoiceRepository {
    private final SpringDataInvoiceRepository invoiceRepo;

    public InvoiceAdapter(SpringDataInvoiceRepository invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceJpaEntity entity = toEntity(invoice);
        InvoiceJpaEntity saved = invoiceRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return invoiceRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepo.findByInvoiceNumber(invoiceNumber).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findByRentalId(Long rentalId) {
        return invoiceRepo.findByRentalId(rentalId).map(this::toDomain);
    }

    @Override
    public List<Invoice> findByClientId(Long clientId) {
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
        e.setId(inv.getId());
        e.setInvoiceNumber(inv.getInvoiceNumber());
        e.setRentalId(inv.getRentalId());
        e.setClientId(inv.getClientId());
        e.setClientName(inv.getClientName());
        e.setClientTaxId(inv.getClientTaxId());
        e.setIssueDate(inv.getIssueDate());
        e.setDueDate(inv.getDueDate());
        e.setSubtotal(inv.getSubtotal());
        e.setTaxAmount(inv.getTaxAmount());
        e.setDepositAmount(inv.getDepositAmount());
        e.setTotalAmount(inv.getTotalAmount());
        e.setPaid(inv.isPaid());
        e.setPaymentDate(inv.getPaymentDate());
        e.setPdfGeneratedAt(inv.getPdfGeneratedAt());

        if (inv.getItems() != null) {
            List<InvoiceItemJpaEntity> itemEntities = new ArrayList<>();
            for (InvoiceItem item : inv.getItems()) {
                InvoiceItemJpaEntity ie = new InvoiceItemJpaEntity();
                if (inv.getId() != null) {
                    ie.setId(item.getId());
                }
                ie.setToolId(item.getToolId());
                ie.setToolName(item.getToolName());
                ie.setDailyRate(item.getDailyRate());
                ie.setDays(item.getDays());
                ie.setSubtotal(item.getSubtotal());
                itemEntities.add(ie);
            }
            e.setItems(itemEntities);
        }
        return e;
    }

    private Invoice toDomain(InvoiceJpaEntity e) {
        if (e == null) return null;
        Invoice inv = new Invoice();
        inv.setId(e.getId());
        inv.setInvoiceNumber(e.getInvoiceNumber());
        inv.setRentalId(e.getRentalId());
        inv.setClientId(e.getClientId());
        inv.setClientName(e.getClientName());
        inv.setClientTaxId(e.getClientTaxId());
        inv.setIssueDate(e.getIssueDate());
        inv.setDueDate(e.getDueDate());
        inv.setSubtotal(e.getSubtotal());
        inv.setTaxAmount(e.getTaxAmount());
        inv.setDepositAmount(e.getDepositAmount());
        inv.setTotalAmount(e.getTotalAmount());
        inv.setPaid(e.isPaid());
        inv.setPaymentDate(e.getPaymentDate());
        inv.setPdfGeneratedAt(e.getPdfGeneratedAt());

        if (e.getItems() != null) {
            List<InvoiceItem> items = new ArrayList<>();
            for (InvoiceItemJpaEntity ie : e.getItems()) {
                InvoiceItem item = new InvoiceItem(ie.getId(), ie.getToolId(), ie.getToolName(), ie.getDailyRate(), ie.getDays());
                items.add(item);
            }
            inv.setItems(items);
        }
        return inv;
    }
}
