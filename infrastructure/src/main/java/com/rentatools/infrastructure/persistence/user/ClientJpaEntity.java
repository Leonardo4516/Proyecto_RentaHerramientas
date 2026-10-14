package com.rentatools.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class ClientJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "document_number", unique = true, nullable = false, length = 50)
    private String documentNumber;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public ClientJpaEntity() {}

    public ClientJpaEntity(UUID id, UUID userId, String documentType, String documentNumber, String address, Double lat, Double lng) {
        this.id = id;
        this.userId = userId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.address = address;
        this.lat = lat != null ? BigDecimal.valueOf(lat) : null;
        this.lng = lng != null ? BigDecimal.valueOf(lng) : null;
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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
