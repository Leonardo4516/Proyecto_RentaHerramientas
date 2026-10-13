package com.rentatools.application.billing.dto;

import java.math.BigDecimal;

public class InvoiceItemDto {
    private Long toolId;
    private String toolName;
    private BigDecimal dailyRate;
    private int days;

    public InvoiceItemDto() {}

    public InvoiceItemDto(Long toolId, String toolName, BigDecimal dailyRate, int days) {
        this.toolId = toolId;
        this.toolName = toolName;
        this.dailyRate = dailyRate;
        this.days = days;
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
}
