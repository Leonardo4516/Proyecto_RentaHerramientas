package com.rentatools.application.tool.dto;

import com.rentatools.domain.tool.ToolCategory;
public record ToolCategoryResponse(Long id, String name, String description, String iconName, boolean active) {
    public static ToolCategoryResponse fromDomain(ToolCategory category) {
        return new ToolCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIconName(),
                category.isActive()
        );
    }
}
