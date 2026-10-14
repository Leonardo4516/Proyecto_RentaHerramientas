package com.rentatools.infrastructure.persistence.billing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class InvoiceJpaEntity {

    @Id
    private UUID id;

    @Column(name = "rental_id", nullable = false)
    private UUID rentalId;

    @Column(name = "payment_id", unique = true)
    private UUID paymentId;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "issued_at")
    private Instant issuedAt = Instant.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deposit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "pdf_storage_path", length = 255)
    private String pdfStoragePath;

    public InvoiceJpaEntity() {}

    public InvoiceJpaEntity(UUID id, UUID rentalId, UUID paymentId, String invoiceNumber, BigDecimal subtotal, BigDecimal tax, BigDecimal deposit, BigDecimal total) {
        this.id = id;
        this.rentalId = rentalId;
        this.paymentId = paymentId;
        this.invoiceNumber = invoiceNumber;
        this.subtotal = subtotal;
        this.tax = tax;
        this.deposit = deposit;
        this.total = total;
        this.issuedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
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

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPdfStoragePath() {
        return pdfStoragePath;
    }

    public void setPdfStoragePath(String pdfStoragePath) {
        this.pdfStoragePath = pdfStoragePath;
    }

    public boolean isPaid() {
        return paymentId != null;
    }

    public BigDecimal getTotalAmount() {
        return total != null ? total : BigDecimal.ZERO;
    }
}
