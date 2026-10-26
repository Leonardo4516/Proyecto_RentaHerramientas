-- ==============================================================================
-- V5__tool_maintenance_logs.sql
-- Bitácora de Mantenimiento Preventivo y Correctivo de Maquinaria
-- ==============================================================================

CREATE TABLE IF NOT EXISTS tool_maintenance_logs (
    id BIGSERIAL PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    technician_name VARCHAR(100),
    cost NUMERIC(12, 2) NOT NULL DEFAULT 0,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    next_maintenance_date DATE,
    notes TEXT,
    CONSTRAINT fk_tool_maintenance FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tool_maintenance_tool_id ON tool_maintenance_logs(tool_id);
