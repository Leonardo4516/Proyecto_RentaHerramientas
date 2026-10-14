package com.rentatools.domain.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceItem {
    private Long id;
    private Long toolId;
    private String toolName;
    private BigDecimal dailyRate;
    private int days;
    private BigDecimal subtotal;

    public InvoiceItem() {}

    public InvoiceItem(Long id, Long toolId, String toolName, BigDecimal dailyRate, int days) {
        this.id = id;
        this.toolId = toolId;
        this.toolName = toolName;
        this.dailyRate = dailyRate != null ? dailyRate : BigDecimal.ZERO;
        this.days = Math.max(1, days);
        this.subtotal = this.dailyRate.multiply(BigDecimal.valueOf(this.days)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
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
