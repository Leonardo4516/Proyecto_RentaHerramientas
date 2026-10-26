package com.rentatools.application.report.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardMetricsResponse {
    private BigDecimal totalRevenue;
    private long activeRentals;
    private long totalUsers;
    private long totalEquipment;
    private double occupancyRate;
    private List<Map<String, Object>> topTools;
    private List<Map<String, Object>> monthlyRevenue;
    private List<Map<String, Object>> recentActivities;

    public DashboardMetricsResponse() {}

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getActiveRentals() {
        return activeRentals;
    }

    public void setActiveRentals(long activeRentals) {
        this.activeRentals = activeRentals;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(long totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public List<Map<String, Object>> getTopTools() {
        return topTools;
    }

    public void setTopTools(List<Map<String, Object>> topTools) {
        this.topTools = topTools;
    }

    public List<Map<String, Object>> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<Map<String, Object>> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public List<Map<String, Object>> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<Map<String, Object>> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
