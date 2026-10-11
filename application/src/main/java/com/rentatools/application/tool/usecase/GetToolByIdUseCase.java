package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.tool.ToolRepository;
import java.util.UUID;

public class GetToolByIdUseCase {
    private final ToolRepository toolRepository;

    public GetToolByIdUseCase(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public ToolResponse execute(UUID id) {
        return toolRepository.findById(id)
                .map(ToolResponse::fromDomain)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + id));
    }
}
