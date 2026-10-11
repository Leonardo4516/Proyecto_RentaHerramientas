package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;
import java.util.List;
import java.util.UUID;

public class ListToolsUseCase {
    private final ToolRepository toolRepository;

    public ListToolsUseCase(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public List<ToolResponse> execute(UUID categoryId, ToolStatus status) {
        if (categoryId != null) {
            return toolRepository.findByCategoryId(categoryId).stream()
                    .filter(t -> status == null || t.getStatus() == status)
                    .map(ToolResponse::fromDomain)
                    .toList();
        }
        if (status != null) {
            return toolRepository.findByStatus(status).stream()
                    .map(ToolResponse::fromDomain)
                    .toList();
        }
        return toolRepository.findAll().stream()
                .map(ToolResponse::fromDomain)
                .toList();
    }
}
