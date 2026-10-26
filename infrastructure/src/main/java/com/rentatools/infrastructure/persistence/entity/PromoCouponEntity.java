package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "promo_coupons")
public class PromoCouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public PromoCouponEntity() {}

    public PromoCouponEntity(String code, BigDecimal discountPercentage, BigDecimal maxDiscountAmount, LocalDate validUntil, Boolean isActive) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.maxDiscountAmount = maxDiscountAmount;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public LocalDate getValidUntil() { return validUntil; }
    public Boolean getIsActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }

    public void setCode(String code) { this.code = code; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public void setIsActive(Boolean active) { isActive = active; }
}
