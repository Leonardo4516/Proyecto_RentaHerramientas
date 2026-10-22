package com.rentatools.application.report.dto;

import java.math.BigDecimal;

public class TopPerformanceResponse {
    private String topToolName;
    private long topToolRentalsCount;
    private BigDecimal topToolRevenue;
    private String topClientName;
    private BigDecimal topClientSpend;
    private double occupancyRate;
    private BigDecimal totalRevenue;
    private long activeRentalsCount;
    private long totalToolsCount;

    public TopPerformanceResponse() {}

    public TopPerformanceResponse(String topToolName, long topToolRentalsCount, BigDecimal topToolRevenue,
                                  String topClientName, BigDecimal topClientSpend, double occupancyRate,
                                  BigDecimal totalRevenue, long activeRentalsCount, long totalToolsCount) {
        this.topToolName = topToolName;
        this.topToolRentalsCount = topToolRentalsCount;
        this.topToolRevenue = topToolRevenue;
        this.topClientName = topClientName;
        this.topClientSpend = topClientSpend;
        this.occupancyRate = occupancyRate;
        this.totalRevenue = totalRevenue;
        this.activeRentalsCount = activeRentalsCount;
        this.totalToolsCount = totalToolsCount;
    }

    public String getTopToolName() { return topToolName; }
    public void setTopToolName(String topToolName) { this.topToolName = topToolName; }

    public long getTopToolRentalsCount() { return topToolRentalsCount; }
    public void setTopToolRentalsCount(long topToolRentalsCount) { this.topToolRentalsCount = topToolRentalsCount; }

    public BigDecimal getTopToolRevenue() { return topToolRevenue; }
    public void setTopToolRevenue(BigDecimal topToolRevenue) { this.topToolRevenue = topToolRevenue; }

    public String getTopClientName() { return topClientName; }
    public void setTopClientName(String topClientName) { this.topClientName = topClientName; }

    public BigDecimal getTopClientSpend() { return topClientSpend; }
    public void setTopClientSpend(BigDecimal topClientSpend) { this.topClientSpend = topClientSpend; }

    public double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(double occupancyRate) { this.occupancyRate = occupancyRate; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getActiveRentalsCount() { return activeRentalsCount; }
    public void setActiveRentalsCount(long activeRentalsCount) { this.activeRentalsCount = activeRentalsCount; }

    public long getTotalToolsCount() { return totalToolsCount; }
    public void setTotalToolsCount(long totalToolsCount) { this.totalToolsCount = totalToolsCount; }
}
