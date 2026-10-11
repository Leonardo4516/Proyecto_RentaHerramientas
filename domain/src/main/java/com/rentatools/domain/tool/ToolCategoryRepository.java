package com.rentatools.domain.tool;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToolCategoryRepository {
    ToolCategory save(ToolCategory category);
    Optional<ToolCategory> findById(UUID id);
    List<ToolCategory> findAllActive();
}
