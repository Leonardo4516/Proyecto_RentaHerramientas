package com.rentatools.domain.user;

import com.rentatools.domain.common.BaseEntity;

public class ProviderProfile extends BaseEntity<Long> {
    private String businessName;
    private String taxId;
    private String commercialRegistry;
    private String address;
    private String city;
    private Double rating;
    private Boolean verified;

    public ProviderProfile() {
        super();
        this.rating = 5.0;
        this.verified = false;
    }

    public ProviderProfile(Long id, String businessName, String taxId, String commercialRegistry, String address, String city, Double rating, Boolean verified) {
        super(id);
        this.businessName = businessName;
        this.taxId = taxId;
        this.commercialRegistry = commercialRegistry;
        this.address = address;
        this.city = city;
        this.rating = rating != null ? rating : 5.0;
        this.verified = verified != null ? verified : false;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getCommercialRegistry() {
        return commercialRegistry;
    }

    public void setCommercialRegistry(String commercialRegistry) {
        this.commercialRegistry = commercialRegistry;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
