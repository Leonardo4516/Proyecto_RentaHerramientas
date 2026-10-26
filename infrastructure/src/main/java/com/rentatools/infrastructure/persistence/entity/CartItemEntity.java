package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_user_tool", columnNames = {"user_id", "tool_id"})
})
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "days", nullable = false)
    private Integer days = 3;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public CartItemEntity() {}

    public CartItemEntity(Long userId, Long toolId, Integer days, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.toolId = toolId;
        this.days = days != null && days > 0 ? days : 3;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
