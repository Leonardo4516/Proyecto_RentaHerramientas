package com.rentatools.domain.user;

import com.rentatools.domain.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

public class ClientProfile extends BaseEntity<UUID> {
    private UUID userId;
    private String documentType;
    private String documentNumber;
    private String address;
    private Double lat;
    private Double lng;

    // Campos complementarios
    private String companyName;
    private String city;
    private String preferredPaymentMethod;

    public ClientProfile() {
        super();
    }

    public ClientProfile(UUID id, UUID userId, String documentType, String documentNumber, String address, Double lat, Double lng) {
        super(id);
        this.userId = userId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDocumentType() {
        return documentType != null ? documentType : "NIT";
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber != null ? documentNumber : "";
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxId() {
        return documentNumber != null ? documentNumber : "";
    }

    public void setTaxId(String taxId) {
        this.documentNumber = taxId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(String preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }
}

