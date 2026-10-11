package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tool_images")
public class ToolImageEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private ToolEntity tool;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_cover")
    private boolean isCover = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public ToolImageEntity() {}

    public ToolImageEntity(UUID id, ToolEntity tool, String imageUrl, boolean isCover) {
        this.id = id;
        this.tool = tool;
        this.imageUrl = imageUrl;
        this.isCover = isCover;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ToolEntity getTool() { return tool; }
    public void setTool(ToolEntity tool) { this.tool = tool; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isCover() { return isCover; }
    public void setCover(boolean cover) { isCover = cover; }
    public Instant getCreatedAt() { return createdAt; }
}
