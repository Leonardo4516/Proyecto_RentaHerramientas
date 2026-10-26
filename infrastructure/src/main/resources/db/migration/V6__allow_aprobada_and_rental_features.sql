-- V6: Actualizar restricción de estado en rentals para permitir 'APROBADA' y soporte de cupones y reseñas
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'rentals'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) LIKE '%status%IN%'
    ) LOOP
        EXECUTE 'ALTER TABLE rentals DROP CONSTRAINT IF EXISTS ' || quote_ident(r.conname);
    END LOOP;
END $$;

ALTER TABLE rentals ADD CONSTRAINT rentals_status_check 
    CHECK (status IN ('PENDIENTE', 'APROBADA', 'CONFIRMADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA', 'RECHAZADA'));

-- Tabla para Cupones de descuento promocionales
CREATE TABLE IF NOT EXISTS promo_coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percentage NUMERIC(5,2) NOT NULL,
    max_discount_amount NUMERIC(12,2) DEFAULT 500000.00,
    valid_until DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO promo_coupons (code, discount_percentage, max_discount_amount, valid_until, is_active)
VALUES 
    ('PROMO10', 10.00, 200000.00, '2026-12-31', TRUE),
    ('OBRA2026', 15.00, 500000.00, '2026-12-31', TRUE),
    ('DESCUENTO20', 20.00, 800000.00, '2026-12-31', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Tabla para Reseñas y Calificaciones de Herramientas
CREATE TABLE IF NOT EXISTS tool_reviews (
    id BIGSERIAL PRIMARY KEY,
    rental_id BIGINT REFERENCES rentals(id) ON DELETE SET NULL,
    tool_id BIGINT NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    client_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
