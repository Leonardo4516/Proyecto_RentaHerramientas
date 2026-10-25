package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tool_reviews")
public class ToolReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rental_id")
    private Long rentalId;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public ToolReviewEntity() {}

    public ToolReviewEntity(Long rentalId, Long toolId, Long clientId, Integer rating, String comment) {
        this.rentalId = rentalId;
        this.toolId = toolId;
        this.clientId = clientId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getRentalId() { return rentalId; }
    public Long getToolId() { return toolId; }
    public Long getClientId() { return clientId; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }

    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
}
