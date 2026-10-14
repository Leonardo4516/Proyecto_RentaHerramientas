package com.rentatools.domain.billing;

import com.rentatools.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Invoice extends BaseEntity<UUID> {
    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");

    private String invoiceNumber;
    private UUID rentalId;
    private UUID paymentId;
    private Instant issuedAt = Instant.now();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal deposit = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private String pdfStoragePath;

    // Campos auxiliares / transitivos
    private UUID clientId;
    private String clientName;
    private String clientTaxId;
    private LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
    private boolean paid = false;
    private LocalDateTime paymentDate;
    private LocalDateTime pdfGeneratedAt;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {
        super();
        this.issuedAt = Instant.now();
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.deposit = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.paid = false;
        this.items = new ArrayList<>();
    }

    public Invoice(UUID id, String invoiceNumber, UUID rentalId, UUID paymentId, BigDecimal subtotal, BigDecimal tax, BigDecimal deposit, BigDecimal total) {
        super(id);
        this.invoiceNumber = invoiceNumber;
        this.rentalId = rentalId;
        this.paymentId = paymentId;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.tax = tax != null ? tax : BigDecimal.ZERO;
        this.deposit = deposit != null ? deposit : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.issuedAt = Instant.now();
        this.items = new ArrayList<>();
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
        this.tax = this.subtotal.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dep = this.deposit != null ? this.deposit : BigDecimal.ZERO;
        this.total = this.subtotal.add(this.tax).add(dep).setScale(2, RoundingMode.HALF_UP);
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

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
        if (paymentId != null) {
            this.paid = true;
        }
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getIssueDate() {
        return issuedAt != null ? LocalDateTime.ofInstant(issuedAt, ZoneId.systemDefault()) : LocalDateTime.now();
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issuedAt = issueDate != null ? issueDate.atZone(ZoneId.systemDefault()).toInstant() : Instant.now();
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

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTaxAmount() {
        return tax;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.tax = taxAmount;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
        recalculateTotals();
    }

    public BigDecimal getDepositAmount() {
        return deposit;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.deposit = depositAmount;
        recalculateTotals();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalAmount() {
        return total;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.total = totalAmount;
    }

    public String getPdfStoragePath() {
        return pdfStoragePath;
    }

    public void setPdfStoragePath(String pdfStoragePath) {
        this.pdfStoragePath = pdfStoragePath;
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

    public boolean isPaid() {
        return paid || paymentId != null;
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

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotals();
    }
}
