package com.rentatools.domain.tool;

import java.util.List;
import java.util.Optional;
public interface ToolCategoryRepository {
    ToolCategory save(ToolCategory category);
    Optional<ToolCategory> findById(Long id);
    List<ToolCategory> findAllActive();
}
