package com.rentatools.domain.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class InvoiceItem {
    private UUID id;
    private UUID toolId;
    private String toolName;
    private BigDecimal dailyRate;
    private int days;
    private BigDecimal subtotal;

    public InvoiceItem() {}

    public InvoiceItem(UUID id, UUID toolId, String toolName, BigDecimal dailyRate, int days) {
        this.id = id;
        this.toolId = toolId;
        this.toolName = toolName;
        this.dailyRate = dailyRate != null ? dailyRate : BigDecimal.ZERO;
        this.days = Math.max(1, days);
        this.subtotal = this.dailyRate.multiply(BigDecimal.valueOf(this.days)).setScale(2, RoundingMode.HALF_UP);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getToolId() {
        return toolId;
    }

    public void setToolId(UUID toolId) {
        this.toolId = toolId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
