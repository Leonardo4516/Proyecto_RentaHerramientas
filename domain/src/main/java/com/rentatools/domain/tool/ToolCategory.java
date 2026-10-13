package com.rentatools.domain.tool;

public class ToolCategory {
    private final Long id;
    private final String name;
    private final String description;
    private final String iconName;
    private final boolean active;

    public ToolCategory(Long id, String name, String description, String iconName, boolean active) {
        this.id = id != null ? id : null;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public boolean isActive() { return active; }
}
