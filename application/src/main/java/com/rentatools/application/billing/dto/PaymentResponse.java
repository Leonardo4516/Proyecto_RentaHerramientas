package com.rentatools.application.billing.dto;

import com.rentatools.domain.billing.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long invoiceId;
    private Long rentalId;
    private Long clientId;
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
        r.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        r.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        r.setTransactionReference(payment.getTransactionReference());
        r.setPaymentDate(payment.getPaymentDate());
        r.setNotes(payment.getNotes());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
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
