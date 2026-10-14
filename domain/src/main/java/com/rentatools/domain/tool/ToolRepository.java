package com.rentatools.domain.tool;

import java.util.List;
import java.util.Optional;
public interface ToolRepository {
    Tool save(Tool tool);
    Optional<Tool> findById(Long id);
    Optional<Tool> findByCode(String code);
    List<Tool> findAll();
    List<Tool> findByCategoryId(Long categoryId);
    List<Tool> findByProviderId(Long providerId);
    List<Tool> findByStatus(ToolStatus status);
}
