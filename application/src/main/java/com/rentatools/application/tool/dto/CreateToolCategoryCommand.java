package com.rentatools.application.tool.dto;

public record CreateToolCategoryCommand(
        String name,
        String description,
        String iconName,
        Boolean active
) {
    public CreateToolCategoryCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (active == null) {
            active = true;
        }
    }
}
