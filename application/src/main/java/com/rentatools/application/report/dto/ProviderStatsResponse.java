package com.rentatools.application.report.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProviderStatsResponse {
    private Long providerId;
    private String businessName;
    private long totalTools;
    private long activeRentals;
    private BigDecimal monthlyEarnings;
    private double rating;
    private List<Map<String, Object>> rentalHistory;

    public ProviderStatsResponse() {}

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public long getTotalTools() {
        return totalTools;
    }

    public void setTotalTools(long totalTools) {
        this.totalTools = totalTools;
    }

    public long getActiveRentals() {
        return activeRentals;
    }

    public void setActiveRentals(long activeRentals) {
        this.activeRentals = activeRentals;
    }

    public BigDecimal getMonthlyEarnings() {
        return monthlyEarnings;
    }

    public void setMonthlyEarnings(BigDecimal monthlyEarnings) {
        this.monthlyEarnings = monthlyEarnings;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<Map<String, Object>> getRentalHistory() {
        return rentalHistory;
    }

    public void setRentalHistory(List<Map<String, Object>> rentalHistory) {
        this.rentalHistory = rentalHistory;
    }
}
