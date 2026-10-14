package com.rentatools.domain.billing;

import com.rentatools.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class Payment extends BaseEntity<UUID> {
    private UUID rentalId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;
    private String status = "PAGADO";
    private Instant paidAt = Instant.now();

    // Campos auxiliares / transitivos
    private UUID invoiceId;
    private UUID clientId;
    private String notes;

    public Payment() {
        super();
        this.status = "PAGADO";
        this.paidAt = Instant.now();
    }

    public Payment(UUID id, UUID rentalId, BigDecimal amount, String paymentMethod, String status, String transactionReference) {
        super(id);
        this.rentalId = rentalId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status != null ? status : "PAGADO";
        this.transactionReference = transactionReference;
        this.paidAt = Instant.now();
    }

    public UUID getRentalId() {
        return rentalId;
    }

    public void setRentalId(UUID rentalId) {
        this.rentalId = rentalId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getPaymentDate() {
        return paidAt != null ? LocalDateTime.ofInstant(paidAt, ZoneId.systemDefault()) : LocalDateTime.now();
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paidAt = paymentDate != null ? paymentDate.atZone(ZoneId.systemDefault()).toInstant() : Instant.now();
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

