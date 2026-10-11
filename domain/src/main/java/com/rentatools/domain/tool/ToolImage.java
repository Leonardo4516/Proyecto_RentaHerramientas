package com.rentatools.domain.tool;

import java.util.UUID;

public class ToolImage {
    private final UUID id;
    private final String imageUrl;
    private final boolean isCover;

    public ToolImage(UUID id, String imageUrl, boolean isCover) {
        this.id = id != null ? id : UUID.randomUUID();
        this.imageUrl = imageUrl;
        this.isCover = isCover;
    }

    public UUID getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public boolean isCover() { return isCover; }
}
