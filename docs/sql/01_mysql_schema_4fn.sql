-- ============================================================================
-- PLATAFORMA DE RENTA DE HERRAMIENTAS Y EQUIPOS DE CONSTRUCCIÓN - RENTATOOLS
-- Script DDL en MySQL 8.0+ / MariaDB - Normalización en 4FN
-- ============================================================================

CREATE DATABASE IF NOT EXISTS renta_herramientas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE renta_herramientas_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- PLATAFORMA DE RENTA DE HERRAMIENTAS Y EQUIPOS DE CONSTRUCCIÓN
-- Script DDL en PostgreSQL 16 - Normalización en 4FN con Claves Numéricas BIGINT (15 dígitos)
-- ============================================================================

-- 1. ROLES DEL SISTEMA (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 2. USUARIOS (Credenciales y datos de identidad base)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(60) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. ASIGNACIÓN DE ROLES (RBAC)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. PERFIL DE ADMINISTRADORES
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    department VARCHAR(100) DEFAULT 'OPERACIONES',
    can_audit BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 5. PERFIL DE CLIENTES (Datos exclusivos del contratista / particular)
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(50) UNIQUE NOT NULL,
    address VARCHAR(255),
    city VARCHAR(100),
    preferred_payment_method VARCHAR(50) DEFAULT 'TARJETA_CREDITO',
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 6. PERFIL DE PROVEEDORES (Datos exclusivos de la empresa de maquinaria)
CREATE TABLE IF NOT EXISTS providers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(50) UNIQUE NOT NULL,
    commercial_registry VARCHAR(100),
    commercial_address VARCHAR(255),
    city VARCHAR(100),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    rating DECIMAL(3, 2) DEFAULT 5.00 CHECK (rating >= 0 AND rating <= 5.00),
    verified BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 7. CATEGORÍAS DE HERRAMIENTAS Y EQUIPOS
CREATE TABLE IF NOT EXISTS tool_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_name VARCHAR(50),
    active BOOLEAN DEFAULT TRUE
);

-- 8. HERRAMIENTAS Y EQUIPOS DE CONSTRUCCIÓN
CREATE TABLE IF NOT EXISTS tools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES tool_categories(id),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    daily_price DECIMAL(12, 2) NOT NULL CHECK (daily_price > 0),
    deposit_amount DECIMAL(12, 2) NOT NULL CHECK (deposit_amount >= 0),
    status VARCHAR(30) DEFAULT 'DISPONIBLE' CHECK (status IN ('DISPONIBLE', 'ALQUILADO', 'EN_MANTENIMIENTO', 'DE_BAJA')),
    accumulated_rental_days INT DEFAULT 0 CHECK (accumulated_rental_days >= 0),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 9. FOTOS DE HERRAMIENTAS (Rutas locales funcionales)
CREATE TABLE IF NOT EXISTS tool_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_cover BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 10. ESPECIFICACIONES TÉCNICAS
CREATE TABLE IF NOT EXISTS tool_specifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    spec_name VARCHAR(100) NOT NULL,
    spec_value VARCHAR(255) NOT NULL
);

-- 11. RESERVAS Y ALQUILERES
CREATE TABLE IF NOT EXISTS rentals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    tool_id BIGINT NOT NULL REFERENCES tools(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL CHECK (total_days > 0),
    daily_rate DECIMAL(12, 2) NOT NULL CHECK (daily_rate > 0),
    deposit_amount DECIMAL(12, 2) NOT NULL CHECK (deposit_amount >= 0),
    subtotal DECIMAL(12, 2) NOT NULL CHECK (subtotal > 0),
    status VARCHAR(30) DEFAULT 'PENDIENTE' CHECK (status IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA', 'RECHAZADA')),
    delivery_address VARCHAR(255),
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 12. DEVOLUCIÓN E INSPECCIÓN FÍSICA
CREATE TABLE IF NOT EXISTS rental_returns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_id BIGINT UNIQUE NOT NULL REFERENCES rentals(id) ON DELETE CASCADE,
    return_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    inspector_user_id BIGINT REFERENCES users(id),
    equipment_condition VARCHAR(50) NOT NULL,
    has_damage BOOLEAN DEFAULT FALSE,
    deposit_refunded DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (deposit_refunded >= 0),
    notes TEXT
);

-- 13. REPORTES DE DAÑOS Y PENALIZACIONES
CREATE TABLE IF NOT EXISTS return_damages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_return_id BIGINT NOT NULL REFERENCES rental_returns(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    penalty_fee DECIMAL(12, 2) NOT NULL CHECK (penalty_fee >= 0),
    evidence_url TEXT,
    reported_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 14. FACTURACIÓN ELECTRÓNICA Y COMPROBANTES
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    rental_id BIGINT REFERENCES rentals(id),
    client_id BIGINT REFERENCES clients(id),
    client_name VARCHAR(150),
    client_tax_id VARCHAR(50),
    issue_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATETIME,
    subtotal DECIMAL(12, 2) NOT NULL CHECK (subtotal >= 0),
    tax_amount DECIMAL(12, 2) NOT NULL CHECK (tax_amount >= 0),
    deposit_amount DECIMAL(12, 2) NOT NULL CHECK (deposit_amount >= 0),
    total_amount DECIMAL(12, 2) NOT NULL CHECK (total_amount >= 0),
    paid BOOLEAN DEFAULT FALSE,
    payment_date DATETIME,
    pdf_generated_at DATETIME,
    pdf_storage_path VARCHAR(255)
);

-- 15. ÍTEMS DETALLADOS DE FACTURA
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    tool_id BIGINT REFERENCES tools(id),
    tool_name VARCHAR(150) NOT NULL,
    daily_rate DECIMAL(12, 2) NOT NULL CHECK (daily_rate > 0),
    days INT NOT NULL CHECK (days > 0),
    subtotal DECIMAL(12, 2) NOT NULL CHECK (subtotal > 0)
);

-- 16. PAGOS REGISTRADOS
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT REFERENCES invoices(id),
    rental_id BIGINT REFERENCES rentals(id),
    client_id BIGINT REFERENCES clients(id),
    amount DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(30) DEFAULT 'PAGADO' CHECK (status IN ('PENDIENTE', 'PAGADO', 'RECHAZADO', 'REEMBOLSADO')),
    transaction_reference VARCHAR(100) UNIQUE NOT NULL,
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- ÍNDICES ESTRATÉGICOS PARA RENDIMIENTO Y CONSULTAS
CREATE INDEX IF NOT EXISTS idx_tools_category ON tools(category_id);
CREATE INDEX IF NOT EXISTS idx_tools_provider ON tools(provider_id);
CREATE INDEX IF NOT EXISTS idx_tools_status ON tools(status);
CREATE INDEX IF NOT EXISTS idx_tools_location ON tools(lat, lng);
CREATE INDEX IF NOT EXISTS idx_rentals_tool_dates ON rentals(tool_id, start_date, end_date, status);
CREATE INDEX IF NOT EXISTS idx_rentals_client ON rentals(client_id);
CREATE INDEX IF NOT EXISTS idx_invoices_rental ON invoices(rental_id);
CREATE INDEX IF NOT EXISTS idx_payments_rental ON payments(rental_id);
CREATE INDEX IF NOT EXISTS idx_payments_invoice ON payments(invoice_id);


-- ==============================================================================
-- V3__rental_audit_timeline.sql
-- Trazabilidad y Auditoría de Estados del Ciclo de Vida de Alquileres
-- ==============================================================================

CREATE TABLE IF NOT EXISTS rental_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_id BIGINT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rental_audit FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_rental_audit_rental_id ON rental_audit_logs(rental_id);


-- ==============================================================================
-- V4__cart_persistence.sql
-- Persistencia del Carrito de Alquiler de Maquinaria por Usuario (Cloud Cart)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL,
    days INT NOT NULL DEFAULT 3,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_tool FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    CONSTRAINT chk_cart_item_days CHECK (days > 0),
    
    CONSTRAINT uk_cart_user_tool UNIQUE (user_id, tool_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_tool_id ON cart_items(tool_id);


SET FOREIGN_KEY_CHECKS = 1;
