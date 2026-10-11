package com.rentatools.application.tool.dto;

import com.rentatools.domain.tool.ToolCategory;
import java.util.UUID;

public record ToolCategoryResponse(UUID id, String name, String description, String iconName, boolean active) {
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
