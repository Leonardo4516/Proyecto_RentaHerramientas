package com.rentatools.domain.tool;

import java.util.List;

public interface ToolMaintenanceRepository {
    ToolMaintenanceLog save(ToolMaintenanceLog log);
    List<ToolMaintenanceLog> findByToolId(Long toolId);
}
