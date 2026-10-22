package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.CreateToolCategoryCommand;
import com.rentatools.application.tool.dto.ToolCategoryResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.tool.ToolCategory;
import com.rentatools.domain.tool.ToolCategoryRepository;

public class CreateToolCategoryUseCase {

    private final ToolCategoryRepository categoryRepository;

    public CreateToolCategoryUseCase(ToolCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ToolCategoryResponse execute(CreateToolCategoryCommand command) {
        if (command == null || command.name() == null || command.name().isBlank()) {
            throw new BusinessRuleValidationException("El nombre de la categoría es obligatorio.");
        }

        ToolCategory category = new ToolCategory(
                null,
                command.name().trim(),
                command.description() != null ? command.description().trim() : null,
                command.iconName() != null ? command.iconName().trim() : "fa-tools",
                Boolean.TRUE.equals(command.active())
        );

        ToolCategory saved = categoryRepository.save(category);
        return ToolCategoryResponse.fromDomain(saved);
    }
}
