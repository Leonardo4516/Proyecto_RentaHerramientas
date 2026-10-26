package com.rentatools.domain.tool;

public class ToolImage {
    private final Long id;
    private final String imageUrl;
    private final boolean isCover;

    public ToolImage(Long id, String imageUrl, boolean isCover) {
        this.id = id != null ? id : null;
        this.imageUrl = imageUrl;
        this.isCover = isCover;
    }

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public boolean isCover() { return isCover; }
}
