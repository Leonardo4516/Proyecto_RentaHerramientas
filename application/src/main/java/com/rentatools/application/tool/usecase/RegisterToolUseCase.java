package com.rentatools.application.tool.usecase;

import com.rentatools.application.tool.dto.RegisterToolCommand;
import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.tool.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RegisterToolUseCase {
    private final ToolRepository toolRepository;

    public RegisterToolUseCase(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public ToolResponse execute(RegisterToolCommand command) {
        if (toolRepository.findByCode(command.code()).isPresent()) {
            throw new BusinessRuleValidationException("Ya existe una herramienta con el código: " + command.code());
        }

        GeoCoordinates coords = (command.latitude() != null && command.longitude() != null)
                ? new GeoCoordinates(command.latitude(), command.longitude())
                : null;

        List<ToolImage> images = new ArrayList<>();
        if (command.imageUrls() != null) {
            boolean first = true;
            for (String url : command.imageUrls()) {
                images.add(new ToolImage(null, url, first));
                first = false;
            }
        }

        List<ToolSpecification> specs = new ArrayList<>();
        if (command.specifications() != null) {
            for (var s : command.specifications()) {
                specs.add(new ToolSpecification(null, s.key(), s.value()));
            }
        }

        Tool tool = new Tool(
                null,
                command.providerId(),
                command.categoryId(),
                command.code(),
                command.name(),
                command.brand(),
                command.model(),
                command.dailyPrice(),
                command.depositAmount(),
                ToolStatus.DISPONIBLE,
                0,
                coords,
                Instant.now(),
                images,
                specs
        );

        Tool saved = toolRepository.save(tool);
        return ToolResponse.fromDomain(saved);
    }
}
