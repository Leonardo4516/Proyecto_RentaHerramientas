package com.rentatools.domain.tool;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToolRepository {
    Tool save(Tool tool);
    Optional<Tool> findById(UUID id);
    Optional<Tool> findByCode(String code);
    List<Tool> findAll();
    List<Tool> findByCategoryId(UUID categoryId);
    List<Tool> findByProviderId(UUID providerId);
    List<Tool> findByStatus(ToolStatus status);
}
