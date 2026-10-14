package com.rentatools.application.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceItemDto {
    private UUID toolId;
    private String toolName;
    private BigDecimal dailyRate;
    private int days;

    public InvoiceItemDto() {}

    public InvoiceItemDto(UUID toolId, String toolName, BigDecimal dailyRate, int days) {
        this.toolId = toolId;
        this.toolName = toolName;
        this.dailyRate = dailyRate;
        this.days = days;
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
}
