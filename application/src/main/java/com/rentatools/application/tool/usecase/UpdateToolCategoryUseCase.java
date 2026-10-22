package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.ToolCategoryResponse;
import com.rentatools.application.tool.dto.UpdateToolCategoryCommand;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.tool.ToolCategory;
import com.rentatools.domain.tool.ToolCategoryRepository;

public class UpdateToolCategoryUseCase {

    private final ToolCategoryRepository categoryRepository;

    public UpdateToolCategoryUseCase(ToolCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ToolCategoryResponse execute(Long id, UpdateToolCategoryCommand command) {
        if (id == null) {
            throw new BusinessRuleValidationException("El ID de la categoría es obligatorio.");
        }
        if (command == null || command.name() == null || command.name().isBlank()) {
            throw new BusinessRuleValidationException("El nombre de la categoría es obligatorio.");
        }

        ToolCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + id));

        ToolCategory updated = new ToolCategory(
                existing.getId(),
                command.name().trim(),
                command.description() != null ? command.description().trim() : existing.getDescription(),
                command.iconName() != null ? command.iconName().trim() : existing.getIconName(),
                command.active() != null ? command.active() : existing.isActive()
        );

        ToolCategory saved = categoryRepository.save(updated);
        return ToolCategoryResponse.fromDomain(saved);
    }
}
