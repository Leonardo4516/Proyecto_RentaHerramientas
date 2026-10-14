package com.rentatools.application.billing.dto;

import com.rentatools.domain.billing.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {
    private UUID id;
    private UUID invoiceId;
    private UUID rentalId;
    private UUID clientId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionReference;
    private LocalDateTime paymentDate;
    private String notes;

    public PaymentResponse() {}

    public static PaymentResponse fromDomain(Payment payment) {
        if (payment == null) return null;
        PaymentResponse r = new PaymentResponse();
        r.setId(payment.getId());
        r.setInvoiceId(payment.getInvoiceId());
        r.setRentalId(payment.getRentalId());
        r.setClientId(payment.getClientId());
        r.setAmount(payment.getAmount());
        r.setPaymentMethod(payment.getPaymentMethod());
        r.setStatus(payment.getStatus());
        r.setTransactionReference(payment.getTransactionReference());
        r.setPaymentDate(payment.getPaymentDate());
        r.setNotes(payment.getNotes());
        return r;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
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

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
