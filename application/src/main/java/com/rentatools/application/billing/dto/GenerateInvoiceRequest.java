package com.rentatools.application.billing.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GenerateInvoiceRequest {
    private Long rentalId;
    private Long clientId;
    private String clientName;
    private String clientTaxId;
    private BigDecimal depositAmount;
    private List<InvoiceItemDto> items = new ArrayList<>();

    public GenerateInvoiceRequest() {}

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

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public List<InvoiceItemDto> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
