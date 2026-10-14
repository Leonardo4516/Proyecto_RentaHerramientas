package com.rentatools.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "providers")
public class ProviderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "tax_id", unique = true, nullable = false, length = 50)
    private String taxId;

    @Column(name = "commercial_address", length = 255)
    private String commercialAddress;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public ProviderJpaEntity() {}

    public ProviderJpaEntity(UUID id, UUID userId, String companyName, String taxId, String commercialAddress, Double lat, Double lng, Double rating) {
        this.id = id;
        this.userId = userId;
        this.companyName = companyName;
        this.taxId = taxId;
        this.commercialAddress = commercialAddress;
        this.lat = lat != null ? BigDecimal.valueOf(lat) : null;
        this.lng = lng != null ? BigDecimal.valueOf(lng) : null;
        this.rating = rating != null ? BigDecimal.valueOf(rating) : new BigDecimal("5.00");
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Double getLat() {
        return lat != null ? lat.doubleValue() : null;
    }

    public void setLat(Double lat) {
        this.lat = lat != null ? BigDecimal.valueOf(lat) : null;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng != null ? lng.doubleValue() : null;
    }

    public void setLng(Double lng) {
        this.lng = lng != null ? BigDecimal.valueOf(lng) : null;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public Double getRating() {
        return rating != null ? rating.doubleValue() : null;
    }

    public void setRating(Double rating) {
        this.rating = rating != null ? BigDecimal.valueOf(rating) : new BigDecimal("5.00");
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
