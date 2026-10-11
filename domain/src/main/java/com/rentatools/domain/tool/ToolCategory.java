package com.rentatools.domain.tool;

import java.util.UUID;

public class ToolCategory {
    private final UUID id;
    private final String name;
    private final String description;
    private final String iconName;
    private final boolean active;

    public ToolCategory(UUID id, String name, String description, String iconName, boolean active) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public boolean isActive() { return active; }
}
