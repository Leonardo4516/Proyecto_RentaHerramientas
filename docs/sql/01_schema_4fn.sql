-- ============================================================================
-- PLATAFORMA DE RENTA DE HERRAMIENTAS Y EQUIPOS DE CONSTRUCCIÓN
-- Script DDL en PostgreSQL 16 - Normalización Estricta en 4FN (Cuarta Forma Normal)
-- ============================================================================

-- Habilitar extensión para UUIDs nativos
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ROLES DEL SISTEMA (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 2. USUARIOS (Credenciales y datos atómicos)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 3. ASIGNACIÓN DE ROLES (4FN: Desacopla la relación N:M de roles independientes)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. PERFIL DE CLIENTES (Datos específicos del contratista/particular)
CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(50) UNIQUE NOT NULL,
    address VARCHAR(255),
    lat NUMERIC(10, 7),
    lng NUMERIC(10, 7),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 5. PERFIL DE PROVEEDORES (Datos de empresa de maquinaria)
CREATE TABLE IF NOT EXISTS providers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(50) UNIQUE NOT NULL,
    commercial_address VARCHAR(255),
    lat NUMERIC(10, 7),
    lng NUMERIC(10, 7),
    rating NUMERIC(3, 2) DEFAULT 5.00 CHECK (rating >= 0 AND rating <= 5.00),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 6. CATEGORÍAS DE HERRAMIENTAS Y EQUIPOS
CREATE TABLE IF NOT EXISTS tool_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_name VARCHAR(50),
    active BOOLEAN DEFAULT TRUE
);

-- 7. HERRAMIENTAS Y EQUIPOS DE CONSTRUCCIÓN (Atributos atómicos 4FN)
CREATE TABLE IF NOT EXISTS tools (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES tool_categories(id),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    daily_price NUMERIC(12, 2) NOT NULL CHECK (daily_price > 0),
    deposit_amount NUMERIC(12, 2) NOT NULL CHECK (deposit_amount >= 0),
    status VARCHAR(30) DEFAULT 'DISPONIBLE' CHECK (status IN ('DISPONIBLE', 'ALQUILADO', 'EN_MANTENIMIENTO', 'DE_BAJA')),
    accumulated_rental_days INT DEFAULT 0 CHECK (accumulated_rental_days >= 0),
    lat NUMERIC(10, 7),
    lng NUMERIC(10, 7),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 8. FOTOS DE HERRAMIENTAS (4FN: Desacopla MVD de imágenes independientes)
CREATE TABLE IF NOT EXISTS tool_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tool_id UUID NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_cover BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 9. ESPECIFICACIONES TÉCNICAS (4FN: Desacopla MVD de ficha técnica independiente)
CREATE TABLE IF NOT EXISTS tool_specifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tool_id UUID NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    spec_name VARCHAR(100) NOT NULL,
    spec_value VARCHAR(255) NOT NULL
);

-- 10. RESERVAS Y ALQUILERES
CREATE TABLE IF NOT EXISTS rentals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES clients(id),
    tool_id UUID NOT NULL REFERENCES tools(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL CHECK (total_days > 0),
    daily_rate NUMERIC(12, 2) NOT NULL CHECK (daily_rate > 0),
    deposit_amount NUMERIC(12, 2) NOT NULL CHECK (deposit_amount >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal > 0),
    status VARCHAR(30) DEFAULT 'PENDIENTE' CHECK (status IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA', 'RECHAZADA')),
    delivery_address VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rental_dates CHECK (end_date >= start_date)
);

-- 11. DEVOLUCIÓN E INSPECCIÓN FÍSICA
CREATE TABLE IF NOT EXISTS rental_returns (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rental_id UUID UNIQUE NOT NULL REFERENCES rentals(id) ON DELETE CASCADE,
    return_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    inspector_user_id UUID REFERENCES users(id),
    equipment_condition VARCHAR(50) NOT NULL,
    has_damage BOOLEAN DEFAULT FALSE,
    deposit_refunded NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (deposit_refunded >= 0),
    notes TEXT
);

-- 12. REPORTES DE DAÑOS Y PENALIZACIONES (4FN: Desacopla MVD de averías múltiples)
CREATE TABLE IF NOT EXISTS return_damages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rental_return_id UUID NOT NULL REFERENCES rental_returns(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    penalty_fee NUMERIC(12, 2) NOT NULL CHECK (penalty_fee >= 0),
    evidence_url TEXT,
    reported_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 13. PAGOS REGISTRADOS
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rental_id UUID NOT NULL REFERENCES rentals(id),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(50) NOT NULL,
    transaction_reference VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(30) DEFAULT 'PAGADO' CHECK (status IN ('PENDIENTE', 'PAGADO', 'RECHAZADO', 'REEMBOLSADO')),
    paid_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 14. FACTURACIÓN ELECTRÓNICA Y COMPROBANTES FISCALES
CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rental_id UUID UNIQUE NOT NULL REFERENCES rentals(id),
    payment_id UUID UNIQUE REFERENCES payments(id),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    issued_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    tax NUMERIC(12, 2) NOT NULL CHECK (tax >= 0),
    deposit NUMERIC(12, 2) NOT NULL CHECK (deposit >= 0),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    pdf_storage_path VARCHAR(255)
);

-- ÍNDICES ESTRATÉGICOS PARA RENDIMIENTO Y CONSULTAS
CREATE INDEX IF NOT EXISTS idx_tools_category ON tools(category_id);
CREATE INDEX IF NOT EXISTS idx_tools_provider ON tools(provider_id);
CREATE INDEX IF NOT EXISTS idx_tools_status ON tools(status);
CREATE INDEX IF NOT EXISTS idx_tools_location ON tools(lat, lng);
CREATE INDEX IF NOT EXISTS idx_rentals_tool_dates ON rentals(tool_id, start_date, end_date, status);
CREATE INDEX IF NOT EXISTS idx_rentals_client ON rentals(client_id);
