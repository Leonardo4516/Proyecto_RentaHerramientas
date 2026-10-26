package com.rentatools.application.billing.dto;

import java.math.BigDecimal;

public class RefundDepositRequest {
    private Long rentalId;
    private BigDecimal amount;
    private String paymentMethod;
    private String notes;

    public RefundDepositRequest() {}

    public RefundDepositRequest(Long rentalId, BigDecimal amount, String paymentMethod, String notes) {
        this.rentalId = rentalId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
