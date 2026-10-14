package com.rentatools.domain.user;

import com.rentatools.domain.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

public class ProviderProfile extends BaseEntity<UUID> {
    private UUID userId;
    private String companyName;
    private String taxId;
    private String commercialAddress;
    private Double lat;
    private Double lng;
    private Double rating;

    // Campos complementarios
    private String commercialRegistry;
    private Boolean verified = false;
    private String city;

    public ProviderProfile() {
        super();
        this.rating = 5.0;
        this.verified = false;
    }

    public ProviderProfile(UUID id, UUID userId, String companyName, String taxId, String commercialAddress, Double lat, Double lng, Double rating) {
        super(id);
        this.userId = userId;
        this.companyName = companyName;
        this.taxId = taxId;
        this.commercialAddress = commercialAddress;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating != null ? rating : 5.0;
        this.verified = true;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBusinessName() {
        return companyName;
    }

    public void setBusinessName(String businessName) {
        this.companyName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getCommercialAddress() {
        return commercialAddress;
    }

    public void setCommercialAddress(String commercialAddress) {
        this.commercialAddress = commercialAddress;
    }

    public String getAddress() {
        return commercialAddress;
    }

    public void setAddress(String address) {
        this.commercialAddress = address;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getCommercialRegistry() {
        return commercialRegistry;
    }

    public void setCommercialRegistry(String commercialRegistry) {
        this.commercialRegistry = commercialRegistry;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

