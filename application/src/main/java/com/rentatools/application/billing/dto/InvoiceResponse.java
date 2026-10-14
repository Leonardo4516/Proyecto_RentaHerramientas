package com.rentatools.application.billing.dto;

import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private UUID rentalId;
    private UUID clientId;
    private String clientName;
    private String clientTaxId;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal depositAmount;
    private BigDecimal totalAmount;
    private boolean paid;
    private LocalDateTime paymentDate;
    private List<InvoiceItem> items = new ArrayList<>();

    public InvoiceResponse() {}

    public static InvoiceResponse fromDomain(Invoice invoice) {
        if (invoice == null) return null;
        InvoiceResponse r = new InvoiceResponse();
        r.setId(invoice.getId());
        r.setInvoiceNumber(invoice.getInvoiceNumber());
        r.setRentalId(invoice.getRentalId());
        r.setClientId(invoice.getClientId());
        r.setClientName(invoice.getClientName());
        r.setClientTaxId(invoice.getClientTaxId());
        r.setIssueDate(invoice.getIssueDate());
        r.setDueDate(invoice.getDueDate());
        r.setSubtotal(invoice.getSubtotal());
        r.setTaxAmount(invoice.getTaxAmount());
        r.setDepositAmount(invoice.getDepositAmount());
        r.setTotalAmount(invoice.getTotalAmount());
        r.setPaid(invoice.isPaid());
        r.setPaymentDate(invoice.getPaymentDate());
        r.setItems(invoice.getItems());
        return r;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public UUID getRentalId() {
        return rentalId;
    }

    public void setRentalId(UUID rentalId) {
        this.rentalId = rentalId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientTaxId() {
        return clientTaxId;
    }

    public void setClientTaxId(String clientTaxId) {
        this.clientTaxId = clientTaxId;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
