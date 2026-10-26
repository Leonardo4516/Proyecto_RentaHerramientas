package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.application.tool.dto.UpdateToolCommand;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;

public class UpdateToolUseCase {
    private final ToolRepository toolRepository;

    public UpdateToolUseCase(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public ToolResponse execute(Long id, UpdateToolCommand command) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + id));

        tool.updateDetails(
                command.name(),
                command.brand(),
                command.model(),
                command.dailyPrice(),
                command.depositAmount()
        );

        Tool updated = toolRepository.save(tool);
        return ToolResponse.fromDomain(updated);
    }

    public ToolResponse updateStatus(Long id, ToolStatus status) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + id));

        tool.updateStatus(status);
        Tool updated = toolRepository.save(tool);
        return ToolResponse.fromDomain(updated);
    }
}
