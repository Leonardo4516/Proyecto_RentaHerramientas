package com.rentatools.domain.billing;

import com.rentatools.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice extends BaseEntity<Long> {
    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");

    private String invoiceNumber;
    private Long rentalId;
    private Long clientId;
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
    private LocalDateTime pdfGeneratedAt;
    private String pdfStoragePath;
    private List<InvoiceItem> items;

    public Invoice() {
        super();
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.depositAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paid = false;
        this.items = new ArrayList<>();
        this.issueDate = LocalDateTime.now();
        this.dueDate = LocalDateTime.now().plusDays(3);
    }

    public Invoice(Long id, String invoiceNumber, Long rentalId, Long clientId, String clientName, String clientTaxId, BigDecimal depositAmount) {
        super(id);
        this.invoiceNumber = invoiceNumber;
        this.rentalId = rentalId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientTaxId = clientTaxId;
        this.depositAmount = depositAmount != null ? depositAmount : BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paid = false;
        this.items = new ArrayList<>();
        this.issueDate = LocalDateTime.now();
        this.dueDate = LocalDateTime.now().plusDays(3);
    }

    public void addItem(InvoiceItem item) {
        if (item != null) {
            this.items.add(item);
            recalculateTotals();
        }
    }

    public void recalculateTotals() {
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (InvoiceItem item : this.items) {
            if (item.getSubtotal() != null) {
                calculatedSubtotal = calculatedSubtotal.add(item.getSubtotal());
            }
        }
        this.subtotal = calculatedSubtotal.setScale(2, RoundingMode.HALF_UP);
        this.taxAmount = this.subtotal.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deposit = this.depositAmount != null ? this.depositAmount : BigDecimal.ZERO;
        this.totalAmount = this.subtotal.add(this.taxAmount).add(deposit).setScale(2, RoundingMode.HALF_UP);
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
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
        recalculateTotals();
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
        if (paid && this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getPdfGeneratedAt() {
        return pdfGeneratedAt;
    }

    public void setPdfGeneratedAt(LocalDateTime pdfGeneratedAt) {
        this.pdfGeneratedAt = pdfGeneratedAt;
    }

    public String getPdfStoragePath() {
        return pdfStoragePath;
    }

    public void setPdfStoragePath(String pdfStoragePath) {
        this.pdfStoragePath = pdfStoragePath;
    }

    public boolean isCancelled() {
        return pdfStoragePath != null && pdfStoragePath.startsWith("CANCELADA");
    }

    public void cancel(String reason) {
        this.paid = false;
        this.pdfStoragePath = "CANCELADA: " + (reason != null && !reason.isBlank() ? reason : "Nota de crédito / Anulación emitida");
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotals();
    }
}
