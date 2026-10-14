package com.rentatools.infrastructure.persistence.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "tool_categories")
public class ToolCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    private boolean active = true;

    public ToolCategoryEntity() {}

    public ToolCategoryEntity(Long id, String name, String description, String iconName, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
