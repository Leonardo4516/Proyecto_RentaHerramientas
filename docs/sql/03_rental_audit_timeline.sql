-- ==============================================================================
-- 03_rental_audit_timeline.sql
-- Trazabilidad y Auditoría de Estados del Ciclo de Vida de Alquileres
-- ==============================================================================

CREATE TABLE IF NOT EXISTS rental_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rental_audit FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_rental_audit_rental_id ON rental_audit_logs(rental_id);
