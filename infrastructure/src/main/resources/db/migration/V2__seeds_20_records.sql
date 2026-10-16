-- ============================================================================
-- PLATAFORMA DE RENTA DE HERRAMIENTAS - SEMILLAS CON IDENTIFICADORES CORTOS (1, 2, 3...)
-- ============================================================================

-- 1. ROLES
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Administrador general de la plataforma'),
(2, 'ROLE_PROVEEDOR', 'Proveedor y dueño de maquinaria y herramientas'),
(3, 'ROLE_CLIENTE', 'Cliente y contratista que alquila herramientas')
ON CONFLICT (id) DO NOTHING;

-- 2. USUARIOS (Credenciales base sin campos nulos)
INSERT INTO users (id, username, email, password_hash, full_name, phone, role, active) VALUES
(1, 'admin', 'admin@rentatools.com', '$2a$10$6eZjcGJq.81SQK.vCKE8ku/fI8K.IdEgbAE7oiANyIVDE0Za0o4o2', 'Carlos Mendoza (Admin)', '+573001112233', 'ROLE_ADMIN', TRUE),
(2, 'superadmin', 'superadmin@rentatools.com', '$2a$10$6eZjcGJq.81SQK.vCKE8ku/fI8K.IdEgbAE7oiANyIVDE0Za0o4o2', 'Laura Restrepo (Supervisor)', '+573001112244', 'ROLE_ADMIN', TRUE),
(3, 'proveedor', 'proveedor1@rentatools.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Representante Maquinaria Andina SAS', '+573100000003', 'ROLE_PROVEEDOR', TRUE),
(4, 'proveedor2', 'proveedor2@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Equipos del Valle Ltda', '+573100000004', 'ROLE_PROVEEDOR', TRUE),
(5, 'proveedor3', 'proveedor3@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Rentas de Occidente SAS', '+573100000005', 'ROLE_PROVEEDOR', TRUE),
(6, 'proveedor4', 'proveedor4@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante ConstruAlquiler Caribe', '+573100000006', 'ROLE_PROVEEDOR', TRUE),
(7, 'proveedor5', 'proveedor5@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Maquinaria Pesada del Este', '+573100000007', 'ROLE_PROVEEDOR', TRUE),
(8, 'proveedor6', 'proveedor6@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Herramientas Industriales Sur', '+573100000008', 'ROLE_PROVEEDOR', TRUE),
(9, 'proveedor7', 'proveedor7@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante MegaEquipos Colombia', '+573100000009', 'ROLE_PROVEEDOR', TRUE),
(10, 'proveedor8', 'proveedor8@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Equipos y Andamios Eje', '+573100000010', 'ROLE_PROVEEDOR', TRUE),
(11, 'proveedor9', 'proveedor9@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Constructora & Rentas Tolima', '+573100000011', 'ROLE_PROVEEDOR', TRUE),
(12, 'proveedor10', 'proveedor10@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Alquiler Maquinaria Huila', '+573100000012', 'ROLE_PROVEEDOR', TRUE),
(13, 'proveedor11', 'proveedor11@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante RentaMaq Boyacá', '+573100000013', 'ROLE_PROVEEDOR', TRUE),
(14, 'proveedor12', 'proveedor12@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Logística y Equipos Llanos', '+573100000014', 'ROLE_PROVEEDOR', TRUE),
(15, 'proveedor13', 'proveedor13@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Suministros Mineros & Obras', '+573100000015', 'ROLE_PROVEEDOR', TRUE),
(16, 'proveedor14', 'proveedor14@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Equipos del Sinú', '+573100000016', 'ROLE_PROVEEDOR', TRUE),
(17, 'proveedor15', 'proveedor15@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Alquileres Cesar SAS', '+573100000017', 'ROLE_PROVEEDOR', TRUE),
(18, 'proveedor16', 'proveedor16@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Maquinaria del Café', '+573100000018', 'ROLE_PROVEEDOR', TRUE),
(19, 'proveedor17', 'proveedor17@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Rentas Civiles Pereira', '+573100000019', 'ROLE_PROVEEDOR', TRUE),
(20, 'proveedor18', 'proveedor18@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante Equipos Industriales Cúcuta', '+573100000020', 'ROLE_PROVEEDOR', TRUE),
(21, 'proveedor19', 'proveedor19@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante ConstruSoluciones Sabana', '+573100000021', 'ROLE_PROVEEDOR', TRUE),
(22, 'proveedor20', 'proveedor20@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Representante HeavyEquipment Bogotá', '+573100000022', 'ROLE_PROVEEDOR', TRUE),
(23, 'cliente', 'cliente1@rentatools.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Constructora Hábitat', '+573200000023', 'ROLE_CLIENTE', TRUE),
(24, 'cliente2', 'cliente2@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Ingeniería y Pavimentos SAS', '+573200000024', 'ROLE_CLIENTE', TRUE),
(25, 'cliente3', 'cliente3@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Consorcio Vial Pacífico', '+573200000025', 'ROLE_CLIENTE', TRUE),
(26, 'cliente4', 'cliente4@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Edificaciones del Norte', '+573200000026', 'ROLE_CLIENTE', TRUE),
(27, 'cliente5', 'cliente5@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Obras Civiles Santander', '+573200000027', 'ROLE_CLIENTE', TRUE),
(28, 'cliente6', 'cliente6@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Juan Fernando Gómez', '+573200000028', 'ROLE_CLIENTE', TRUE),
(29, 'cliente7', 'cliente7@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Construcciones Modernas SAS', '+573200000029', 'ROLE_CLIENTE', TRUE),
(30, 'cliente8', 'cliente8@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Infraestructura Urbana Ltda', '+573200000030', 'ROLE_CLIENTE', TRUE),
(31, 'cliente9', 'cliente9@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Andrés Felipe Morales', '+573200000031', 'ROLE_CLIENTE', TRUE),
(32, 'cliente10', 'cliente10@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Desarrollos Inmobiliarios Eje', '+573200000032', 'ROLE_CLIENTE', TRUE),
(33, 'cliente11', 'cliente11@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Urbanizadora del Caribe', '+573200000033', 'ROLE_CLIENTE', TRUE),
(34, 'cliente12', 'cliente12@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Camilo Andrés Torres', '+573200000034', 'ROLE_CLIENTE', TRUE),
(35, 'cliente13', 'cliente13@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Constructora Los Andes', '+573200000035', 'ROLE_CLIENTE', TRUE),
(36, 'cliente14', 'cliente14@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Ingenieros Asociados Tolima', '+573200000036', 'ROLE_CLIENTE', TRUE),
(37, 'cliente15', 'cliente15@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Santiago Vega Martínez', '+573200000037', 'ROLE_CLIENTE', TRUE),
(38, 'cliente16', 'cliente16@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Soluciones Viales del Llano', '+573200000038', 'ROLE_CLIENTE', TRUE),
(39, 'cliente17', 'cliente17@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'David Eduardo Castro', '+573200000039', 'ROLE_CLIENTE', TRUE),
(40, 'cliente18', 'cliente18@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Pavimentar Colombia SAS', '+573200000040', 'ROLE_CLIENTE', TRUE),
(41, 'cliente19', 'cliente19@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Gabriel Antonio Rivas', '+573200000041', 'ROLE_CLIENTE', TRUE),
(42, 'cliente20', 'cliente20@rentatools.com', '$2a$10$0Kayc/X2SlppNGmZEkySBOG2TpVyEt7w4CzT7RLjLU2Uf26Z6tEnC', 'Grupo Constructor Prisma', '+573200000042', 'ROLE_CLIENTE', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 3. USER_ROLES
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(2, 1),
(3, 2),
(4, 2),
(5, 2),
(6, 2),
(7, 2),
(8, 2),
(9, 2),
(10, 2),
(11, 2),
(12, 2),
(13, 2),
(14, 2),
(15, 2),
(16, 2),
(17, 2),
(18, 2),
(19, 2),
(20, 2),
(21, 2),
(22, 2),
(23, 3),
(24, 3),
(25, 3),
(26, 3),
(27, 3),
(28, 3),
(29, 3),
(30, 3),
(31, 3),
(32, 3),
(33, 3),
(34, 3),
(35, 3),
(36, 3),
(37, 3),
(38, 3),
(39, 3),
(40, 3),
(41, 3),
(42, 3)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 4. ADMINISTRADORES
INSERT INTO admins (id, user_id, department, can_audit) VALUES
(1, 1, 'DIRECCION_GENERAL', TRUE),
(2, 2, 'SUPERVISION_OPERATIVA', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 5. PROVEEDORES
INSERT INTO providers (id, user_id, company_name, tax_id, commercial_registry, commercial_address, city, lat, lng, rating, verified) VALUES
(1, 3, 'Maquinaria Andina SAS', '900123456-1', 'REG-CAM-0100', 'Cra 68 # 45-12, Bogotá', 'Bogotá', 4.6534, -74.0836, 4.9, TRUE),
(2, 4, 'Equipos del Valle Ltda', '900234567-2', 'REG-CAM-0200', 'Calle 15 # 28-30, Cali', 'Cali', 3.4516, -76.532, 4.8, TRUE),
(3, 5, 'Rentas de Occidente SAS', '900345678-3', 'REG-CAM-0300', 'Cra 50 # 10-20, Medellín', 'Medellín', 6.2442, -75.5812, 4.7, TRUE),
(4, 6, 'ConstruAlquiler Caribe', '900456789-4', 'REG-CAM-0400', 'Calle 72 # 54-10, Barranquilla', 'Barranquilla', 10.9685, -74.7813, 5.0, TRUE),
(5, 7, 'Maquinaria Pesada del Este', '900567890-5', 'REG-CAM-0500', 'Cra 27 # 36-15, Bucaramanga', 'Bucaramanga', 7.1254, -73.1198, 4.6, TRUE),
(6, 8, 'Herramientas Industriales Sur', '900678901-6', 'REG-CAM-0600', 'Cra 10 # 12-40, Pasto', 'Pasto', 1.2136, -77.2811, 4.8, TRUE),
(7, 9, 'MegaEquipos Colombia', '900789012-7', 'REG-CAM-0700', 'Av Boyacá # 64-20, Bogotá', 'Bogotá', 4.6923, -74.0921, 4.9, TRUE),
(8, 10, 'Equipos y Andamios Eje', '900890123-8', 'REG-CAM-0800', 'Av Santander # 45-10, Manizales', 'Manizales', 5.0689, -75.5174, 4.5, TRUE),
(9, 11, 'Constructora & Rentas Tolima', '900901234-9', 'REG-CAM-0900', 'Calle 60 # 12-18, Ibagué', 'Ibagué', 4.4389, -75.2322, 4.7, TRUE),
(10, 12, 'Alquiler Maquinaria Huila', '901012345-0', 'REG-CAM-1000', 'Cra 5 # 10-30, Neiva', 'Neiva', 2.9273, -75.2819, 4.6, TRUE),
(11, 13, 'RentaMaq Boyacá', '901123456-1', 'REG-CAM-1100', 'Calle 20 # 9-45, Tunja', 'Tunja', 5.5353, -73.3678, 4.8, TRUE),
(12, 14, 'Logística y Equipos Llanos', '901234567-2', 'REG-CAM-1200', 'Cra 40 # 25-10, Villavicencio', 'Villavicencio', 4.142, -73.6266, 4.9, TRUE),
(13, 15, 'Suministros Mineros & Obras', '901345678-3', 'REG-CAM-1300', 'Av Las Vegas # 18-50, Envigado', 'Envigado', 6.1759, -75.5917, 5.0, TRUE),
(14, 16, 'Equipos del Sinú', '901456789-4', 'REG-CAM-1400', 'Calle 29 # 4-20, Montería', 'Montería', 8.7479, -75.8814, 4.4, TRUE),
(15, 17, 'Alquileres Cesar SAS', '901567890-5', 'REG-CAM-1500', 'Cra 9 # 15-30, Valledupar', 'Valledupar', 10.4631, -73.2532, 4.7, TRUE),
(16, 18, 'Maquinaria del Café', '901678901-6', 'REG-CAM-1600', 'Cra 14 # 21-15, Armenia', 'Armenia', 4.5339, -75.6811, 4.8, TRUE),
(17, 19, 'Rentas Civiles Pereira', '901789012-7', 'REG-CAM-1700', 'Av 30 de Agosto # 40-10, Pereira', 'Pereira', 4.8133, -75.6961, 4.9, TRUE),
(18, 20, 'Equipos Industriales Cúcuta', '901890123-8', 'REG-CAM-1800', 'Av Gran Colombia # 10-25, Cúcuta', 'Cúcuta', 7.8939, -72.5078, 4.6, TRUE),
(19, 21, 'ConstruSoluciones Sabana', '901901234-9', 'REG-CAM-1900', 'Autopista Norte Km 21, Chía', 'Chía', 4.8622, -74.0583, 4.8, TRUE),
(20, 22, 'HeavyEquipment Bogotá', '902012345-0', 'REG-CAM-2000', 'Calle 13 # 86-40, Bogotá', 'Bogotá', 4.6582, -74.1354, 5.0, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 6. CLIENTES
INSERT INTO clients (id, user_id, document_type, document_number, address, city, preferred_payment_method, lat, lng) VALUES
(1, 23, 'NIT', '901111222-1', 'Cra 15 # 85-30, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6712, -74.0589),
(2, 24, 'NIT', '901222333-2', 'Calle 50 # 40-12, Medellín', 'Medellín', 'TARJETA_CREDITO', 6.2518, -75.5636),
(3, 25, 'NIT', '901333444-3', 'Av 4N # 20-45, Cali', 'Cali', 'TARJETA_CREDITO', 3.4612, -76.5289),
(4, 26, 'NIT', '901444555-4', 'Cra 53 # 74-12, Barranquilla', 'Barranquilla', 'TARJETA_CREDITO', 10.9981, -74.8012),
(5, 27, 'NIT', '901555666-5', 'Calle 35 # 20-10, Bucaramanga', 'Bucaramanga', 'TARJETA_CREDITO', 7.1198, -73.1254),
(6, 28, 'CC', '1020304050', 'Calle 127 # 45-10, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.7123, -74.0532),
(7, 29, 'NIT', '901666777-6', 'Cra 7 # 116-50, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6989, -74.0321),
(8, 30, 'NIT', '901777888-7', 'Calle 10 # 43-20, Medellín', 'Medellín', 'TARJETA_CREDITO', 6.2089, -75.5689),
(9, 31, 'CC', '1030405060', 'Cra 66 # 9-15, Cali', 'Cali', 'TARJETA_CREDITO', 3.3989, -76.5412),
(10, 32, 'NIT', '901888999-8', 'Av Centenario # 15-20, Armenia', 'Armenia', 'TARJETA_CREDITO', 4.5412, -75.6689),
(11, 33, 'NIT', '901999000-9', 'Calle 82 # 51-20, Barranquilla', 'Barranquilla', 'TARJETA_CREDITO', 11.0089, -74.8189),
(12, 34, 'CC', '1040506070', 'Calle 45 # 14-25, Manizales', 'Manizales', 'TARJETA_CREDITO', 5.0612, -75.5089),
(13, 35, 'NIT', '902111222-1', 'Cra 30 # 40-10, Pereira', 'Pereira', 'TARJETA_CREDITO', 4.8112, -75.6889),
(14, 36, 'NIT', '902222333-2', 'Calle 15 # 4-50, Ibagué', 'Ibagué', 'TARJETA_CREDITO', 4.4412, -75.2289),
(15, 37, 'CC', '1050607080', 'Cra 8 # 22-15, Tunja', 'Tunja', 'TARJETA_CREDITO', 5.5389, -73.3589),
(16, 38, 'NIT', '902333444-3', 'Av Circunvalar # 18-30, Villavicencio', 'Villavicencio', 'TARJETA_CREDITO', 4.1489, -73.6189),
(17, 39, 'CC', '1060708090', 'Cra 43A # 1-50, Medellín', 'Medellín', 'TARJETA_CREDITO', 6.2012, -75.5789),
(18, 40, 'NIT', '902444555-4', 'Calle 80 # 68-15, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6889, -74.0889),
(19, 41, 'CC', '1070809010', 'Cra 1 # 10-20, Cartagena', 'Cartagena', 'TARJETA_CREDITO', 10.4212, -75.5489),
(20, 42, 'NIT', '902555666-5', 'Calle 100 # 19-45, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6882, -74.0489)
ON CONFLICT (id) DO NOTHING;

-- 7. CATEGORÍAS DE HERRAMIENTAS
INSERT INTO tool_categories (id, name, description, icon_name, active) VALUES
(1, 'Excavación y Movimiento de Tierras', 'Retroexcavadoras, minicargadores y excavadoras de oruga.', 'truck', TRUE),
(2, 'Concreto y Mezclado', 'Hormigoneras, trompos, vibradores de concreto y reglas vibratorias.', 'cube', TRUE),
(3, 'Trabajo en Altura y Andamios', 'Andamios certificados multidireccionales, plataformas tijera y escaleras.', 'ladder', TRUE),
(4, 'Demolición y Rotura', 'Martillos demoledores neumáticos y electro-neumáticos pesados.', 'hammer', TRUE),
(5, 'Generación de Energía y Plantas Eléctricas', 'Generadores diésel y gasolina de alto rendimiento.', 'zap', TRUE),
(6, 'Corte y Perforación de Suelos', 'Cortadoras de pavimento, taladros diamantados y sacanúcleos.', 'disc', TRUE),
(7, 'Compactación de Terreno', 'Ranas compactadoras, vibrocompactadores de rodillo y bailarinas.', 'activity', TRUE),
(8, 'Bombeo y Evacuación de Agua', 'Motobombas sumergibles y de lodos para achique de zanjas.', 'droplet', TRUE),
(9, 'Compresores y Aire Comprimido', 'Compresores diésel portátiles y líneas de presión neumáticas.', 'wind', TRUE),
(10, 'Elevación y Montacargas', 'Plumas grúa, malacates eléctricos y winches de obra.', 'arrow-up', TRUE),
(11, 'Soldadura Industrial', 'Equipos inversor y plantas motosoldadoras para estructuras de acero.', 'flame', TRUE),
(12, 'Iluminación de Obras Nocturnas', 'Torres de iluminación diésel móviles con mástil telescópico.', 'sun', TRUE),
(13, 'Topografía y Medición Láser', 'Estaciones totales, niveles ópticos y niveles láser autonivelantes.', 'crosshair', TRUE),
(14, 'Pintura y Recubrimientos Airless', 'Equipos de aspersión de pintura de alta presión y revocadoras.', 'feather', TRUE),
(15, 'Limpieza e Hidrolavado Industrial', 'Hidrolavadoras de alta presión de agua fría y caliente.', 'refresh-cw', TRUE),
(16, 'Jardinería y Descapote de Terreno', 'Guadañadoras industriales, motosierras y ahoyadoras a motor.', 'scissors', TRUE),
(17, 'Formaletas y Encofrados Metálicos', 'Módulos de formaleta metálica y puntales telescópicos.', 'grid', TRUE),
(18, 'Herramienta Eléctrica de Taller', 'Tronzadoras, esmeriles angulares y pulidoras industriales.', 'tool', TRUE),
(19, 'Carga y Transporte Ligero', 'Dúmperes de obra y carretillas motorizadas sobre orugas.', 'shopping-cart', TRUE),
(20, 'Seguridad y Señalización Vial', 'Vallas de cerramiento, conos reflectivos y semáforos portátiles.', 'shield', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 8. HERRAMIENTAS
INSERT INTO tools (id, provider_id, category_id, code, name, brand, model, daily_price, deposit_amount, status, accumulated_rental_days, lat, lng) VALUES
(1, 1, 1, 'CAT-320D', 'Retroexcavadora Oruga CAT 320D', 'Caterpillar', '320D', 850000, 2500000, 'DISPONIBLE', 15, 4.6534, -74.0836),
(2, 2, 1, 'BOB-S570', 'Minicargador Bobcat S570', 'Bobcat', 'S570', 350000, 1200000, 'DISPONIBLE', 8, 3.4516, -76.532),
(3, 3, 4, 'BOS-GSH27', 'Martillo Demoledor Eléctrico 30kg', 'Bosch', 'GSH 27 VC', 95000, 300000, 'DISPONIBLE', 4, 6.2442, -75.5812),
(4, 4, 2, 'WAC-CT36', 'Allanadora de Concreto Helicóptero', 'Wacker Neuson', 'CT 36-5A', 120000, 400000, 'DISPONIBLE', 6, 10.9685, -74.7813),
(5, 5, 7, 'MIK-MTX70', 'Bailarina Compactadora Canguro', 'Mikasa', 'MTX70E', 80000, 250000, 'DISPONIBLE', 12, 7.1254, -73.1198),
(6, 6, 5, 'CUM-C55D5', 'Planta Eléctrica Diésel 50kVA Insonorizada', 'Cummins', 'C55D5', 280000, 1000000, 'DISPONIBLE', 20, 1.2136, -77.2811),
(7, 7, 3, 'AND-CERT-01', 'Torre de Andamio Multidireccional 6m', 'Layher', 'Allround', 75000, 200000, 'DISPONIBLE', 30, 4.6923, -74.0921),
(8, 8, 6, 'HUS-FS400', 'Cortadora de Pavimento Disco 14"', 'Husqvarna', 'FS 400 LV', 110000, 350000, 'DISPONIBLE', 9, 5.0689, -75.5174),
(9, 9, 8, 'HON-WT30X', 'Motobomba de Lodos y Sólidos 3"', 'Honda', 'WT30XK4', 85000, 250000, 'DISPONIBLE', 5, 4.4389, -75.2322),
(10, 10, 2, 'KOL-1.5B', 'Mezcladora de Concreto 1.5 Bultos a Gasolina', 'Kollser', 'M150', 65000, 200000, 'DISPONIBLE', 22, 2.9273, -75.2819),
(11, 11, 12, 'GEN-RL4', 'Torre de Iluminación Diésel 4x1000W', 'Generac', 'MLT3060', 160000, 600000, 'DISPONIBLE', 14, 5.5353, -73.3678),
(12, 12, 9, 'SUL-185', 'Compresor de Aire Portátil Diésel 185 CFM', 'Sullair', '185 Tier 4', 220000, 800000, 'DISPONIBLE', 18, 4.142, -73.6266),
(13, 13, 7, 'DYN-CC1200', 'Rodillo Vibrocompactador Doble 1.2 Ton', 'Dynapac', 'CC1200', 310000, 1100000, 'DISPONIBLE', 10, 6.1759, -75.5917),
(14, 14, 13, 'LEI-TS07', 'Estación Total Topográfica Láser', 'Leica Geosystems', 'FlexLine TS07', 190000, 700000, 'DISPONIBLE', 7, 8.7479, -75.8814),
(15, 15, 10, 'GEN-GS1930', 'Plataforma Elevadora Tijera Eléctrica 8m', 'Genie', 'GS-1930', 180000, 650000, 'DISPONIBLE', 16, 10.4631, -73.2532),
(16, 16, 4, 'HIL-TE3000', 'Martillo Rompedor Pesado AVR 1800W', 'Hilti', 'TE 3000-AVR', 130000, 450000, 'DISPONIBLE', 11, 4.5339, -75.6811),
(17, 17, 11, 'MIL-BOBCAT', 'Motosoldadora Diésel Multiproceso 250A', 'Miller', 'Bobcat 250', 175000, 600000, 'DISPONIBLE', 13, 4.8133, -75.6961),
(18, 18, 15, 'KAR-HD10', 'Hidrolavadora Industrial Alta Presión 3000 PSI', 'Kärcher', 'HD 10/25-4 S', 90000, 300000, 'DISPONIBLE', 9, 7.8939, -72.5078),
(19, 19, 2, 'ENR-RUL', 'Regla Vibratoria de Concreto Telescópica 3m', 'Enar', 'Huracán H', 70000, 220000, 'DISPONIBLE', 6, 4.8622, -74.0583),
(20, 20, 1, 'JCB-3CX', 'Retroexcavadora Mixta JCB 3CX 4x4', 'JCB', '3CX Eco', 720000, 2200000, 'DISPONIBLE', 25, 4.6582, -74.1354),
(21, 1, 6, 'HIL-DD250', 'Perforadora Sacanúcleos Diamante Diam 300mm', 'Hilti', 'DD 250', 140000, 500000, 'DISPONIBLE', 8, 4.6534, -74.0836),
(22, 2, 14, 'GRA-ULTRA', 'Equipo de Pintura Airless Graco UltraMax', 'Graco', 'Ultra Max II 695', 115000, 400000, 'DISPONIBLE', 5, 3.4516, -76.532),
(23, 3, 10, 'MAL-500KG', 'Malacate Eléctrico de Obra 500kg con Brazo', 'Imer', 'Airone 200N', 60000, 180000, 'DISPONIBLE', 14, 6.2442, -75.5812),
(24, 4, 2, 'VIB-CONC-5M', 'Vibrador de Concreto a Gasolina Manguera 5m', 'Wacker Neuson', 'M2500', 45000, 150000, 'DISPONIBLE', 19, 10.9685, -74.7813),
(25, 5, 7, 'RAN-COMP-90', 'Placa Compactadora Reversible 90kg', 'Mikasa', 'MVC-F80R', 85000, 260000, 'DISPONIBLE', 11, 7.1254, -73.1198)
ON CONFLICT (id) DO NOTHING;

-- 9. FOTOS DE HERRAMIENTAS (Rutas locales funcionales SVG)
INSERT INTO tool_images (id, tool_id, image_url, is_cover) VALUES
(1, 1, '/assets/img/machinery/Retroexcavadora-Oruga-CAT-320D.png', TRUE),
(2, 2, '/assets/img/machinery/minicargador-bobcat.jpeg', TRUE),
(3, 3, '/assets/img/machinery/taladro-demoledor-bosch.jpeg', TRUE),
(4, 4, '/assets/img/machinery/allanadora-concreto.jpeg', TRUE),
(5, 5, '/assets/img/machinery/compactadora-canguro.jpg', TRUE),
(6, 6, '/assets/img/machinery/planta-electrica-cummins.jpg', TRUE),
(7, 7, '/assets/img/machinery/andamio-multidireccional.jpg', TRUE),
(8, 8, '/assets/img/machinery/cortadora-pavimento.jpg', TRUE),
(9, 9, '/assets/img/machinery/motobomba-lodos.jpg', TRUE),
(10, 10, '/assets/img/machinery/mezcladora-diesel-380l.jpg', TRUE),
(11, 11, '/assets/img/machinery/torre-iluminacion-generac.jpg', TRUE),
(12, 12, '/assets/img/machinery/compresor-aire.jpg', TRUE),
(13, 13, '/assets/img/machinery/rodillo-compactador.jpg', TRUE),
(14, 14, '/assets/img/machinery/estacion-total.jpg', TRUE),
(15, 15, '/assets/img/machinery/plataforma-tijera.jpg', TRUE),
(16, 16, '/assets/img/machinery/martillo-rompedor-hilti.jpg', TRUE),
(17, 17, '/assets/img/machinery/motosoldadora-miller.jpg', TRUE),
(18, 18, '/assets/img/machinery/hidrolavadora-karcher.jpg', TRUE),
(19, 19, '/assets/img/machinery/ReglaVibratoriaDeConcreto.jpeg', TRUE),
(20, 20, '/assets/img/machinery/jcb-3cx-4x4.jpg', TRUE),
(21, 21, '/assets/img/machinery/perforadora-sacanucleos.jpg', TRUE),
(22, 22, '/assets/img/machinery/equipo-pintura-airless.jpg', TRUE),
(23, 23, '/assets/img/machinery/malacate-electrico.jpg', TRUE),
(24, 24, '/assets/img/machinery/vibrador-concreto.jpg', TRUE),
(25, 25, '/assets/img/machinery/placa-compactadora.jpg', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 10. ESPECIFICACIONES TÉCNICAS
INSERT INTO tool_specifications (id, tool_id, spec_name, spec_value) VALUES
(1, 1, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Retroexcavadora Oruga CAT 320D'),
(2, 2, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Minicargador Bobcat S570'),
(3, 3, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Martillo Demoledor Eléctrico 30kg'),
(4, 4, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Allanadora de Concreto Helicóptero'),
(5, 5, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Bailarina Compactadora Canguro'),
(6, 6, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Planta Eléctrica Diésel 50kVA Insonorizada'),
(7, 7, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Torre de Andamio Multidireccional 6m'),
(8, 8, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Cortadora de Pavimento Disco 14"'),
(9, 9, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Motobomba de Lodos y Sólidos 3"'),
(10, 10, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Mezcladora de Concreto 1.5 Bultos a Gasolina'),
(11, 11, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Torre de Iluminación Diésel 4x1000W'),
(12, 12, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Compresor de Aire Portátil Diésel 185 CFM'),
(13, 13, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Rodillo Vibrocompactador Doble 1.2 Ton'),
(14, 14, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Estación Total Topográfica Láser'),
(15, 15, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Plataforma Elevadora Tijera Eléctrica 8m'),
(16, 16, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Martillo Rompedor Pesado AVR 1800W'),
(17, 17, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Motosoldadora Diésel Multiproceso 250A'),
(18, 18, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Hidrolavadora Industrial Alta Presión 3000 PSI'),
(19, 19, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Regla Vibratoria de Concreto Telescópica 3m'),
(20, 20, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Retroexcavadora Mixta JCB 3CX 4x4'),
(21, 21, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Perforadora Sacanúcleos Diamante Diam 300mm'),
(22, 22, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Equipo de Pintura Airless Graco UltraMax'),
(23, 23, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Malacate Eléctrico de Obra 500kg con Brazo'),
(24, 24, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Vibrador de Concreto a Gasolina Manguera 5m'),
(25, 25, 'Capacidad / Potencia', 'Especificación certificada de fábrica para Placa Compactadora Reversible 90kg')
ON CONFLICT (id) DO NOTHING;

-- 11. RESERVAS Y ALQUILERES
INSERT INTO rentals (id, client_id, tool_id, start_date, end_date, total_days, daily_rate, deposit_amount, subtotal, status, delivery_address) VALUES
(1, 1, 1, '2026-09-03', '2026-09-07', 4, 850000.0, 2500000.0, 3400000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(2, 2, 2, '2026-09-05', '2026-09-10', 5, 350000.0, 1200000.0, 1750000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(3, 3, 3, '2026-09-07', '2026-09-13', 6, 95000.0, 300000.0, 570000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(4, 4, 4, '2026-09-09', '2026-09-16', 7, 120000.0, 400000.0, 840000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(5, 5, 5, '2026-09-11', '2026-09-14', 3, 80000.0, 250000.0, 240000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(6, 6, 6, '2026-09-13', '2026-09-17', 4, 280000.0, 1000000.0, 1120000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(7, 7, 7, '2026-09-15', '2026-09-20', 5, 75000.0, 200000.0, 375000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(8, 8, 8, '2026-09-17', '2026-09-23', 6, 110000.0, 350000.0, 660000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(9, 9, 9, '2026-09-19', '2026-09-26', 7, 85000.0, 250000.0, 595000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(10, 10, 10, '2026-09-21', '2026-09-24', 3, 65000.0, 200000.0, 195000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(11, 11, 11, '2026-09-23', '2026-09-27', 4, 160000.0, 600000.0, 640000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(12, 12, 12, '2026-09-25', '2026-09-30', 5, 220000.0, 800000.0, 1100000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(13, 13, 13, '2026-09-27', '2026-10-03', 6, 310000.0, 1100000.0, 1860000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(14, 14, 14, '2026-09-29', '2026-10-06', 7, 190000.0, 700000.0, 1330000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(15, 15, 15, '2026-10-01', '2026-10-04', 3, 180000.0, 650000.0, 540000.0, 'COMPLETADA', 'Obra Central Sector Norte'),
(16, 16, 16, '2026-10-03', '2026-10-07', 4, 130000.0, 450000.0, 520000.0, 'CONFIRMADA', 'Obra Central Sector Norte'),
(17, 17, 17, '2026-10-05', '2026-10-10', 5, 175000.0, 600000.0, 875000.0, 'CONFIRMADA', 'Obra Central Sector Norte'),
(18, 18, 18, '2026-10-07', '2026-10-13', 6, 90000.0, 300000.0, 540000.0, 'CONFIRMADA', 'Obra Central Sector Norte'),
(19, 19, 19, '2026-10-09', '2026-10-16', 7, 70000.0, 220000.0, 490000.0, 'CONFIRMADA', 'Obra Central Sector Norte'),
(20, 20, 20, '2026-10-11', '2026-10-14', 3, 720000.0, 2200000.0, 2160000.0, 'CONFIRMADA', 'Obra Central Sector Norte'),
(21, 1, 21, '2026-10-13', '2026-10-17', 4, 140000.0, 500000.0, 560000.0, 'PENDIENTE', 'Obra Central Sector Norte'),
(22, 2, 22, '2026-10-15', '2026-10-20', 5, 115000.0, 400000.0, 575000.0, 'PENDIENTE', 'Obra Central Sector Norte'),
(23, 3, 23, '2026-10-17', '2026-10-23', 6, 60000.0, 180000.0, 360000.0, 'PENDIENTE', 'Obra Central Sector Norte'),
(24, 4, 24, '2026-10-19', '2026-10-26', 7, 45000.0, 150000.0, 315000.0, 'PENDIENTE', 'Obra Central Sector Norte'),
(25, 5, 25, '2026-10-21', '2026-10-24', 3, 85000.0, 260000.0, 255000.0, 'PENDIENTE', 'Obra Central Sector Norte')
ON CONFLICT (id) DO NOTHING;

-- 12. DEVOLUCIÓN E INSPECCIÓN FÍSICA
INSERT INTO rental_returns (id, rental_id, inspector_user_id, equipment_condition, has_damage, deposit_refunded, notes) VALUES
(1, 1, 1, 'EXCELENTE', False, 2500000.00, 'Inspección técnica completada.'),
(2, 2, 1, 'EXCELENTE', False, 1200000.00, 'Inspección técnica completada.'),
(3, 3, 1, 'EXCELENTE', False, 300000.00, 'Inspección técnica completada.'),
(4, 4, 1, 'DAÑOS_LEVES', True, 280000.00, 'Inspección técnica completada.'),
(5, 5, 1, 'EXCELENTE', False, 250000.00, 'Inspección técnica completada.'),
(6, 6, 1, 'EXCELENTE', False, 1000000.00, 'Inspección técnica completada.'),
(7, 7, 1, 'EXCELENTE', False, 200000.00, 'Inspección técnica completada.'),
(8, 8, 1, 'DAÑOS_LEVES', True, 245000.00, 'Inspección técnica completada.'),
(9, 9, 1, 'EXCELENTE', False, 250000.00, 'Inspección técnica completada.'),
(10, 10, 1, 'EXCELENTE', False, 200000.00, 'Inspección técnica completada.'),
(11, 11, 1, 'EXCELENTE', False, 600000.00, 'Inspección técnica completada.'),
(12, 12, 1, 'DAÑOS_LEVES', True, 560000.00, 'Inspección técnica completada.'),
(13, 13, 1, 'EXCELENTE', False, 1100000.00, 'Inspección técnica completada.'),
(14, 14, 1, 'EXCELENTE', False, 700000.00, 'Inspección técnica completada.'),
(15, 15, 1, 'EXCELENTE', False, 650000.00, 'Inspección técnica completada.'),
(16, 16, 1, 'DAÑOS_LEVES', True, 315000.00, 'Inspección técnica completada.'),
(17, 17, 1, 'EXCELENTE', False, 600000.00, 'Inspección técnica completada.'),
(18, 18, 1, 'EXCELENTE', False, 300000.00, 'Inspección técnica completada.'),
(19, 19, 1, 'EXCELENTE', False, 220000.00, 'Inspección técnica completada.'),
(20, 20, 1, 'DAÑOS_LEVES', True, 1540000.00, 'Inspección técnica completada.')
ON CONFLICT (id) DO NOTHING;

-- 13. REPORTES DE DAÑOS Y PENALIZACIONES
INSERT INTO return_damages (id, rental_return_id, description, penalty_fee) VALUES
(1, 1, 'Desgaste anormal en orugas / filtro en alquiler #1', 100000.00),
(2, 2, 'Desgaste anormal en orugas / filtro en alquiler #2', 150000.00),
(3, 3, 'Desgaste anormal en orugas / filtro en alquiler #3', 50000.00),
(4, 4, 'Desgaste anormal en orugas / filtro en alquiler #4', 100000.00),
(5, 5, 'Desgaste anormal en orugas / filtro en alquiler #5', 150000.00),
(6, 6, 'Desgaste anormal en orugas / filtro en alquiler #6', 50000.00),
(7, 7, 'Desgaste anormal en orugas / filtro en alquiler #7', 100000.00),
(8, 8, 'Desgaste anormal en orugas / filtro en alquiler #8', 150000.00),
(9, 9, 'Desgaste anormal en orugas / filtro en alquiler #9', 50000.00),
(10, 10, 'Desgaste anormal en orugas / filtro en alquiler #10', 100000.00),
(11, 11, 'Desgaste anormal en orugas / filtro en alquiler #11', 150000.00),
(12, 12, 'Desgaste anormal en orugas / filtro en alquiler #12', 50000.00),
(13, 13, 'Desgaste anormal en orugas / filtro en alquiler #13', 100000.00),
(14, 14, 'Desgaste anormal en orugas / filtro en alquiler #14', 150000.00),
(15, 15, 'Desgaste anormal en orugas / filtro en alquiler #15', 50000.00),
(16, 16, 'Desgaste anormal en orugas / filtro en alquiler #16', 100000.00),
(17, 17, 'Desgaste anormal en orugas / filtro en alquiler #17', 150000.00),
(18, 18, 'Desgaste anormal en orugas / filtro en alquiler #18', 50000.00),
(19, 19, 'Desgaste anormal en orugas / filtro en alquiler #19', 100000.00),
(20, 20, 'Desgaste anormal en orugas / filtro en alquiler #20', 150000.00)
ON CONFLICT (id) DO NOTHING;

-- 14. FACTURAS
INSERT INTO invoices (id, invoice_number, rental_id, client_id, client_name, client_tax_id, subtotal, tax_amount, deposit_amount, total_amount, paid, pdf_storage_path) VALUES
(1, 'FE-2026-00001', 1, 1, 'Constructora Hábitat', '901001-1', 3400000.00, 646000.00, 2500000.00, 6546000.00, TRUE, '/invoices/FE-2026-00001.pdf'),
(2, 'FE-2026-00002', 2, 2, 'Ingeniería y Pavimentos SAS', '901002-1', 1750000.00, 332500.00, 1200000.00, 3282500.00, TRUE, '/invoices/FE-2026-00002.pdf'),
(3, 'FE-2026-00003', 3, 3, 'Consorcio Vial Pacífico', '901003-1', 570000.00, 108300.00, 300000.00, 978300.00, TRUE, '/invoices/FE-2026-00003.pdf'),
(4, 'FE-2026-00004', 4, 4, 'Edificaciones del Norte', '901004-1', 840000.00, 159600.00, 400000.00, 1399600.00, TRUE, '/invoices/FE-2026-00004.pdf'),
(5, 'FE-2026-00005', 5, 5, 'Obras Civiles Santander', '901005-1', 240000.00, 45600.00, 250000.00, 535600.00, TRUE, '/invoices/FE-2026-00005.pdf'),
(6, 'FE-2026-00006', 6, 6, 'Cliente Contratista #6', '901006-1', 1120000.00, 212800.00, 1000000.00, 2332800.00, TRUE, '/invoices/FE-2026-00006.pdf'),
(7, 'FE-2026-00007', 7, 7, 'Cliente Contratista #7', '901007-1', 375000.00, 71250.00, 200000.00, 646250.00, TRUE, '/invoices/FE-2026-00007.pdf'),
(8, 'FE-2026-00008', 8, 8, 'Cliente Contratista #8', '901008-1', 660000.00, 125400.00, 350000.00, 1135400.00, TRUE, '/invoices/FE-2026-00008.pdf'),
(9, 'FE-2026-00009', 9, 9, 'Cliente Contratista #9', '901009-1', 595000.00, 113050.00, 250000.00, 958050.00, TRUE, '/invoices/FE-2026-00009.pdf'),
(10, 'FE-2026-00010', 10, 10, 'Cliente Contratista #10', '901010-1', 195000.00, 37050.00, 200000.00, 432050.00, TRUE, '/invoices/FE-2026-00010.pdf'),
(11, 'FE-2026-00011', 11, 11, 'Cliente Contratista #11', '901011-1', 640000.00, 121600.00, 600000.00, 1361600.00, TRUE, '/invoices/FE-2026-00011.pdf'),
(12, 'FE-2026-00012', 12, 12, 'Cliente Contratista #12', '901012-1', 1100000.00, 209000.00, 800000.00, 2109000.00, TRUE, '/invoices/FE-2026-00012.pdf'),
(13, 'FE-2026-00013', 13, 13, 'Cliente Contratista #13', '901013-1', 1860000.00, 353400.00, 1100000.00, 3313400.00, TRUE, '/invoices/FE-2026-00013.pdf'),
(14, 'FE-2026-00014', 14, 14, 'Cliente Contratista #14', '901014-1', 1330000.00, 252700.00, 700000.00, 2282700.00, TRUE, '/invoices/FE-2026-00014.pdf'),
(15, 'FE-2026-00015', 15, 15, 'Cliente Contratista #15', '901015-1', 540000.00, 102600.00, 650000.00, 1292600.00, TRUE, '/invoices/FE-2026-00015.pdf'),
(16, 'FE-2026-00016', 16, 16, 'Cliente Contratista #16', '901016-1', 520000.00, 98800.00, 450000.00, 1068800.00, TRUE, '/invoices/FE-2026-00016.pdf'),
(17, 'FE-2026-00017', 17, 17, 'Cliente Contratista #17', '901017-1', 875000.00, 166250.00, 600000.00, 1641250.00, TRUE, '/invoices/FE-2026-00017.pdf'),
(18, 'FE-2026-00018', 18, 18, 'Cliente Contratista #18', '901018-1', 540000.00, 102600.00, 300000.00, 942600.00, TRUE, '/invoices/FE-2026-00018.pdf'),
(19, 'FE-2026-00019', 19, 19, 'Cliente Contratista #19', '901019-1', 490000.00, 93100.00, 220000.00, 803100.00, TRUE, '/invoices/FE-2026-00019.pdf'),
(20, 'FE-2026-00020', 20, 20, 'Cliente Contratista #20', '901020-1', 2160000.00, 410400.00, 2200000.00, 4770400.00, TRUE, '/invoices/FE-2026-00020.pdf'),
(21, 'FE-2026-00021', 21, 1, 'Cliente Contratista #21', '901021-1', 560000.00, 106400.00, 500000.00, 1166400.00, TRUE, '/invoices/FE-2026-00021.pdf'),
(22, 'FE-2026-00022', 22, 2, 'Cliente Contratista #22', '901022-1', 575000.00, 109250.00, 400000.00, 1084250.00, TRUE, '/invoices/FE-2026-00022.pdf'),
(23, 'FE-2026-00023', 23, 3, 'Cliente Contratista #23', '901023-1', 360000.00, 68400.00, 180000.00, 608400.00, TRUE, '/invoices/FE-2026-00023.pdf'),
(24, 'FE-2026-00024', 24, 4, 'Cliente Contratista #24', '901024-1', 315000.00, 59850.00, 150000.00, 524850.00, TRUE, '/invoices/FE-2026-00024.pdf'),
(25, 'FE-2026-00025', 25, 5, 'Cliente Contratista #25', '901025-1', 255000.00, 48450.00, 260000.00, 563450.00, TRUE, '/invoices/FE-2026-00025.pdf')
ON CONFLICT (id) DO NOTHING;

-- 15. ÍTEMS DE FACTURA
INSERT INTO invoice_items (id, invoice_id, tool_id, tool_name, daily_rate, days, subtotal) VALUES
(1, 1, 1, 'CAT-320D', 850000.0, 4, 3400000.0),
(2, 2, 2, 'BOB-S570', 350000.0, 5, 1750000.0),
(3, 3, 3, 'BOS-GSH27', 95000.0, 6, 570000.0),
(4, 4, 4, 'WAC-CT36', 120000.0, 7, 840000.0),
(5, 5, 5, 'MIK-MTX70', 80000.0, 3, 240000.0),
(6, 6, 6, 'CUM-C55D5', 280000.0, 4, 1120000.0),
(7, 7, 7, 'AND-CERT-01', 75000.0, 5, 375000.0),
(8, 8, 8, 'HUS-FS400', 110000.0, 6, 660000.0),
(9, 9, 9, 'HON-WT30X', 85000.0, 7, 595000.0),
(10, 10, 10, 'KOL-1.5B', 65000.0, 3, 195000.0),
(11, 11, 11, 'GEN-RL4', 160000.0, 4, 640000.0),
(12, 12, 12, 'SUL-185', 220000.0, 5, 1100000.0),
(13, 13, 13, 'DYN-CC1200', 310000.0, 6, 1860000.0),
(14, 14, 14, 'LEI-TS07', 190000.0, 7, 1330000.0),
(15, 15, 15, 'GEN-GS1930', 180000.0, 3, 540000.0),
(16, 16, 16, 'HIL-TE3000', 130000.0, 4, 520000.0),
(17, 17, 17, 'MIL-BOBCAT', 175000.0, 5, 875000.0),
(18, 18, 18, 'KAR-HD10', 90000.0, 6, 540000.0),
(19, 19, 19, 'ENR-RUL', 70000.0, 7, 490000.0),
(20, 20, 20, 'JCB-3CX', 720000.0, 3, 2160000.0),
(21, 21, 21, 'HIL-DD250', 140000.0, 4, 560000.0),
(22, 22, 22, 'GRA-ULTRA', 115000.0, 5, 575000.0),
(23, 23, 23, 'MAL-500KG', 60000.0, 6, 360000.0),
(24, 24, 24, 'VIB-CONC-5M', 45000.0, 7, 315000.0),
(25, 25, 25, 'RAN-COMP-90', 85000.0, 3, 255000.0)
ON CONFLICT (id) DO NOTHING;

-- 16. PAGOS
INSERT INTO payments (id, invoice_id, rental_id, client_id, amount, payment_method, transaction_reference, status) VALUES
(1, 1, 1, 1, 5900000.00, 'Tarjeta de Crédito', 'TRX-2026-1001', 'PAGADO'),
(2, 2, 2, 2, 2950000.00, 'Transferencia Bancaria', 'TRX-2026-1002', 'PAGADO'),
(3, 3, 3, 3, 870000.00, 'Efectivo', 'TRX-2026-1003', 'PAGADO'),
(4, 4, 4, 4, 1240000.00, 'PSE', 'TRX-2026-1004', 'PAGADO'),
(5, 5, 5, 5, 490000.00, 'Tarjeta de Crédito', 'TRX-2026-1005', 'PAGADO'),
(6, 6, 6, 6, 2120000.00, 'Transferencia Bancaria', 'TRX-2026-1006', 'PAGADO'),
(7, 7, 7, 7, 575000.00, 'Efectivo', 'TRX-2026-1007', 'PAGADO'),
(8, 8, 8, 8, 1010000.00, 'PSE', 'TRX-2026-1008', 'PAGADO'),
(9, 9, 9, 9, 845000.00, 'Tarjeta de Crédito', 'TRX-2026-1009', 'PAGADO'),
(10, 10, 10, 10, 395000.00, 'Transferencia Bancaria', 'TRX-2026-1010', 'PAGADO'),
(11, 11, 11, 11, 1240000.00, 'Efectivo', 'TRX-2026-1011', 'PAGADO'),
(12, 12, 12, 12, 1900000.00, 'PSE', 'TRX-2026-1012', 'PAGADO'),
(13, 13, 13, 13, 2960000.00, 'Tarjeta de Crédito', 'TRX-2026-1013', 'PAGADO'),
(14, 14, 14, 14, 2030000.00, 'Transferencia Bancaria', 'TRX-2026-1014', 'PAGADO'),
(15, 15, 15, 15, 1190000.00, 'Efectivo', 'TRX-2026-1015', 'PAGADO'),
(16, 16, 16, 16, 970000.00, 'PSE', 'TRX-2026-1016', 'PAGADO'),
(17, 17, 17, 17, 1475000.00, 'Tarjeta de Crédito', 'TRX-2026-1017', 'PAGADO'),
(18, 18, 18, 18, 840000.00, 'Transferencia Bancaria', 'TRX-2026-1018', 'PAGADO'),
(19, 19, 19, 19, 710000.00, 'Efectivo', 'TRX-2026-1019', 'PAGADO'),
(20, 20, 20, 20, 4360000.00, 'PSE', 'TRX-2026-1020', 'PAGADO'),
(21, 21, 21, 1, 1060000.00, 'Tarjeta de Crédito', 'TRX-2026-1021', 'PAGADO'),
(22, 22, 22, 2, 975000.00, 'Transferencia Bancaria', 'TRX-2026-1022', 'PAGADO'),
(23, 23, 23, 3, 540000.00, 'Efectivo', 'TRX-2026-1023', 'PAGADO'),
(24, 24, 24, 4, 465000.00, 'PSE', 'TRX-2026-1024', 'PAGADO'),
(25, 25, 25, 5, 515000.00, 'Tarjeta de Crédito', 'TRX-2026-1025', 'PAGADO')
ON CONFLICT (id) DO NOTHING;

-- SINCRONIZACIÓN DE SECUENCIAS AUTO-INCREMENTALES
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('admins', 'id'), COALESCE((SELECT MAX(id) FROM admins), 1));
SELECT setval(pg_get_serial_sequence('providers', 'id'), COALESCE((SELECT MAX(id) FROM providers), 1));
SELECT setval(pg_get_serial_sequence('clients', 'id'), COALESCE((SELECT MAX(id) FROM clients), 1));
SELECT setval(pg_get_serial_sequence('tool_categories', 'id'), COALESCE((SELECT MAX(id) FROM tool_categories), 1));
SELECT setval(pg_get_serial_sequence('tools', 'id'), COALESCE((SELECT MAX(id) FROM tools), 1));
SELECT setval(pg_get_serial_sequence('tool_images', 'id'), COALESCE((SELECT MAX(id) FROM tool_images), 1));
SELECT setval(pg_get_serial_sequence('tool_specifications', 'id'), COALESCE((SELECT MAX(id) FROM tool_specifications), 1));
SELECT setval(pg_get_serial_sequence('rentals', 'id'), COALESCE((SELECT MAX(id) FROM rentals), 1));
SELECT setval(pg_get_serial_sequence('rental_returns', 'id'), COALESCE((SELECT MAX(id) FROM rental_returns), 1));
SELECT setval(pg_get_serial_sequence('return_damages', 'id'), COALESCE((SELECT MAX(id) FROM return_damages), 1));
SELECT setval(pg_get_serial_sequence('invoices', 'id'), COALESCE((SELECT MAX(id) FROM invoices), 1));
SELECT setval(pg_get_serial_sequence('invoice_items', 'id'), COALESCE((SELECT MAX(id) FROM invoice_items), 1));
SELECT setval(pg_get_serial_sequence('payments', 'id'), COALESCE((SELECT MAX(id) FROM payments), 1));