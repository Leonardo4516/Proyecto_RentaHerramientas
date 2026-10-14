package com.rentatools.application.billing.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateInvoiceRequest {
    private UUID rentalId;
    private UUID clientId;
    private String clientName;
    private String clientTaxId;
    private BigDecimal depositAmount;
    private List<InvoiceItemDto> items = new ArrayList<>();

    public GenerateInvoiceRequest() {}

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
