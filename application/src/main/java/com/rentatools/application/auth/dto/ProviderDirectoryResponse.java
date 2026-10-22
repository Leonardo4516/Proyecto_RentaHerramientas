package com.rentatools.application.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProviderDirectoryResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String taxId;
    private String commercialRegistry;
    private String commercialAddress;
    private String city;
    private BigDecimal rating;
    private Boolean verified;
    private int fleetCount;
    private LocalDateTime createdAt;

    public ProviderDirectoryResponse() {}

    public ProviderDirectoryResponse(Long id, Long userId, String companyName, String taxId,
                                   String commercialRegistry, String commercialAddress, String city,
                                   BigDecimal rating, Boolean verified, int fleetCount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.companyName = companyName;
        this.taxId = taxId;
        this.commercialRegistry = commercialRegistry;
        this.commercialAddress = commercialAddress;
        this.city = city;
        this.rating = rating;
        this.verified = verified;
        this.fleetCount = fleetCount;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getCommercialRegistry() { return commercialRegistry; }
    public void setCommercialRegistry(String commercialRegistry) { this.commercialRegistry = commercialRegistry; }

    public String getCommercialAddress() { return commercialAddress; }
    public void setCommercialAddress(String commercialAddress) { this.commercialAddress = commercialAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public int getFleetCount() { return fleetCount; }
    public void setFleetCount(int fleetCount) { this.fleetCount = fleetCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
