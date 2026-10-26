-- ============================================================================
-- PLATAFORMA DE RENTA DE HERRAMIENTAS Y EQUIPOS DE CONSTRUCCIÓN - RENTATOOLS
-- Datos Semilla con Empresas del Sector Construcción en Colombia (MySQL 8.0+)
-- ============================================================================

USE renta_herramientas_db;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- PLATAFORMA DE RENTA DE HERRAMIENTAS Y MAQUINARIA - RENTATOOLS ENTERPRISE
-- SEMILLAS REALES CON EMPRESAS DEL SECTOR CONSTRUCCIÓN EN COLOMBIA
-- ============================================================================

-- 1. ROLES
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Administrador general y supervisor de la plataforma'),
(2, 'ROLE_PROVEEDOR', 'Proveedor y propietario de maquinaria y equipos de construcción'),
(3, 'ROLE_CLIENTE', 'Cliente, constructora o contratista que alquila maquinaria')
ON DUPLICATE KEY UPDATE id=id;

-- 2. USUARIOS
-- Credenciales predeterminadas para inicio de sesión inmediato:
-- Admin:        admin       / Admin123!      (o superadmin / Admin123!)
-- Proveedores:  proveedor   / Proveedor123!  (proveedor2, proveedor3... proveedor20 / Proveedor123!)
-- Clientes:     cliente     / Cliente123!    (cliente2, cliente3... cliente20 / Cliente123!)
INSERT INTO users (id, username, email, password_hash, full_name, phone, role, active) VALUES
-- Administradores
(1, 'admin', 'admin@rentatools.com', '$2a$10$6eZjcGJq.81SQK.vCKE8ku/fI8K.IdEgbAE7oiANyIVDE0Za0o4o2', 'Carlos Mendoza (Director General RentaTools)', '+57 300 111 2233', 'ROLE_ADMIN', TRUE),
(2, 'superadmin', 'superadmin@rentatools.com', '$2a$10$6eZjcGJq.81SQK.vCKE8ku/fI8K.IdEgbAE7oiANyIVDE0Za0o4o2', 'Laura Restrepo (Supervisora Operativa)', '+57 300 111 2244', 'ROLE_ADMIN', TRUE),

-- Proveedores (Empresas Reales de Maquinaria en Colombia)
(3, 'proveedor', 'contacto@maquinariaandina.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Alejandro Morales (Maquinaria Andina SAS)', '+57 310 845 1201', 'ROLE_PROVEEDOR', TRUE),
(4, 'proveedor2', 'rentas@gecolsa.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Dr. Gabriel Jaramillo (Gecolsa Maquinaria Pesada SAS)', '+57 310 932 4502', 'ROLE_PROVEEDOR', TRUE),
(5, 'proveedor3', 'operaciones@rentasantioquia.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Juan Carlos Uribe (Rentas y Equipos de Antioquia SAS)', '+57 311 223 8803', 'ROLE_PROVEEDOR', TRUE),
(6, 'proveedor4', 'alquileres@construcaribe.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Arq. Beatriz Donado (ConstruAlquiler Caribe SAS)', '+57 315 440 1904', 'ROLE_PROVEEDOR', TRUE),
(7, 'proveedor5', 'comercial@maquinariadelvalle.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Andrés Felipe Caicedo (Maquinaria del Valle Ltda)', '+57 312 778 3305', 'ROLE_PROVEEDOR', TRUE),
(8, 'proveedor6', 'equipos@industrialesdeloriente.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Nelson Santander (Herramientas Industriales del Oriente)', '+57 314 556 9906', 'ROLE_PROVEEDOR', TRUE),
(9, 'proveedor7', 'contacto@sunbeltrentals.co', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Rodrigo Cárdenas (Sunbelt Rentals Colombia SAS)', '+57 318 667 4407', 'ROLE_PROVEEDOR', TRUE),
(10, 'proveedor8', 'servicio@andamiosyejecafetero.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Jorge Mario Osorio (Andamios & Equipos Eje Cafetero)', '+57 313 889 1108', 'ROLE_PROVEEDOR', TRUE),
(11, 'proveedor9', 'info@tolimamaquinaria.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Carlos Julio Barreto (Tolima Maquinaria & Construcción)', '+57 317 334 5509', 'ROLE_PROVEEDOR', TRUE),
(12, 'proveedor10', 'rentas@surcolombianahuila.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Fernando Perdomo (Rentas Surcolombiana Huila)', '+57 316 990 2210', 'ROLE_PROVEEDOR', TRUE),
(13, 'proveedor11', 'ventas@rentamaqboyaca.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Héctor Fabio Boyacá (RentaMaq Altiplano Boyacá)', '+57 310 123 7711', 'ROLE_PROVEEDOR', TRUE),
(14, 'proveedor12', 'logistica@equiposdelllano.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Pedro Pablo Baquero (Equipos y Logística del Llano)', '+57 311 889 6612', 'ROLE_PROVEEDOR', TRUE),
(15, 'proveedor13', 'suministros@obrasenvigado.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Mateo Saldarriaga (Suministros Mineros & Obras Envigado)', '+57 315 778 4413', 'ROLE_PROVEEDOR', TRUE),
(16, 'proveedor14', 'gerencia@equiposdelsinu.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Julio César Durango (Equipos del Sinú Montería)', '+57 312 445 1114', 'ROLE_PROVEEDOR', TRUE),
(17, 'proveedor15', 'alquileres@cesaryguajira.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Alfonso Cuello (Alquileres Cesar & Guajira SAS)', '+57 314 223 8815', 'ROLE_PROVEEDOR', TRUE),
(18, 'proveedor16', 'contacto@maquinariadelcafe.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Mauricio Villegas (Maquinaria del Café Armenia)', '+57 318 990 3316', 'ROLE_PROVEEDOR', TRUE),
(19, 'proveedor17', 'operaciones@rentascivilespereira.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Diego Fernando Botero (Rentas Civiles Pereira SAS)', '+57 313 667 9917', 'ROLE_PROVEEDOR', TRUE),
(20, 'proveedor18', 'ventas@equiposcucuta.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Álvaro Peñaranda (Equipos Industriales Cúcuta)', '+57 317 112 4418', 'ROLE_PROVEEDOR', TRUE),
(21, 'proveedor19', 'servicio@sabananorte.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Camilo Esguerra (ConstruSoluciones Sabana Norte)', '+57 316 445 8819', 'ROLE_PROVEEDOR', TRUE),
(22, 'proveedor20', 'rentas@heavyequipmentbogota.com', '$2a$10$ytJom0YRfnJFchSR.MNag.sygBW/V83.owao4bW5jxoxyY4vcEK3G', 'Ing. Luis Alberto Fajardo (HeavyEquipment Bogotá SAS)', '+57 310 778 2220', 'ROLE_PROVEEDOR', TRUE),

-- Clientes (Constructoras y Contratistas Civiles Reales)
(23, 'cliente', 'compras@constructorabolivar.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Cristian Muñoz (Constructora Hábitat SAS)', '+57 320 445 1023', 'ROLE_CLIENTE', TRUE),
(24, 'cliente2', 'proyectos@amarilo.com.co', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Roberto Camargo (Amarilo Construcciones SAS)', '+57 320 889 2024', 'ROLE_CLIENTE', TRUE),
(25, 'cliente3', 'obras@marval.com.co', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Carolina Morales (Marval Obras y Proyectos SAS)', '+57 321 223 3025', 'ROLE_CLIENTE', TRUE),
(26, 'cliente4', 'suministros@arquitecturayconcreto.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Arq. Felipe Restrepo (Arquitectura & Concreto SAS)', '+57 322 556 4026', 'ROLE_CLIENTE', TRUE),
(27, 'cliente5', 'vias@consorciopacifico.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Mauricio Echeverry (Consorcio Vial Pacífico)', '+57 320 778 5027', 'ROLE_CLIENTE', TRUE),
(28, 'cliente6', 'carlos.restrepo.obras@gmail.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Carlos Mario Restrepo (Contratista Civil Independiente)', '+57 319 990 6028', 'ROLE_CLIENTE', TRUE),
(29, 'cliente7', 'mariana.gomez.residente@hotmail.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Mariana Gómez Torres (Ingeniera Residente de Obra)', '+57 318 112 7029', 'ROLE_CLIENTE', TRUE),
(30, 'cliente8', 'maquinaria@cusezar.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Andrés Felipe Salazar (Cusezar Ingeniería & Construcción)', '+57 320 334 8030', 'ROLE_CLIENTE', TRUE),
(31, 'cliente9', 'infraestructura@conconcreto.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Daniel Ospina (Conconcreto Infraestructura SAS)', '+57 321 667 9031', 'ROLE_CLIENTE', TRUE),
(32, 'cliente10', 'equipos@pavimentoscolombia.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Jorge Enrique Pinzón (Pavimentos Colombia SAS)', '+57 322 889 0032', 'ROLE_CLIENTE', TRUE),
(33, 'cliente11', 'desarrollos@prodesa.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Dra. Valentina Castro (Prodesa Desarrollos Inmobiliarios)', '+57 320 123 1033', 'ROLE_CLIENTE', TRUE),
(34, 'cliente12', 'tramo1@metrobogota.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Diego Fernando Meza (Consorcio Metro Línea 1)', '+57 319 445 2034', 'ROLE_CLIENTE', TRUE),
(35, 'cliente13', 'construccion@colpatria.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Laura Camila Cárdenas (Constructora Colpatria SAS)', '+57 318 778 3035', 'ROLE_CLIENTE', TRUE),
(36, 'cliente14', 'proyectos@jaramillomora.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Hernán Darío Caicedo (Jaramillo Mora Constructora)', '+57 320 990 4036', 'ROLE_CLIENTE', TRUE),
(37, 'cliente15', 'david.vega.maestro@gmail.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'David Fernando Vega (Maestro de Obra Certificado)', '+57 321 334 5037', 'ROLE_CLIENTE', TRUE),
(38, 'cliente16', 'operaciones@urbanamineria.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Marcela Pardo (Urbana de Obras y Minería SAS)', '+57 322 778 6038', 'ROLE_CLIENTE', TRUE),
(39, 'cliente17', 'vias@viasdelllano.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Guillermo León Barreto (Ingeniería de Vías del Llano SAS)', '+57 320 223 7039', 'ROLE_CLIENTE', TRUE),
(40, 'cliente18', 'gabriel.rivas.electrico@hotmail.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Gabriel Antonio Rivas (Instalaciones Eléctricas Industriales)', '+57 319 556 8040', 'ROLE_CLIENTE', TRUE),
(41, 'cliente19', 'licitaciones@elcondor.com.co', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Juan Pablo Betancur (Constructora El Cóndor SA)', '+57 318 889 9041', 'ROLE_CLIENTE', TRUE),
(42, 'cliente20', 'obras@edificacionesdelnorte.com', '$2a$10$Zws/PHlS7lMGaotV17jSQ.Ql.6fgZUrX.pkFv.xA..0Lno.0lgky6', 'Ing. Álvaro José De la Rosa (Edificaciones Civiles del Norte)', '+57 320 112 0042', 'ROLE_CLIENTE', TRUE)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    email = VALUES(email),
    password_hash = VALUES(password_hash),
    full_name = VALUES(full_name),
    phone = VALUES(phone),
    role = VALUES(role),
    active = VALUES(active);

-- 3. USER_ROLES
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (2, 1),
(3, 2), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2),
(11, 2), (12, 2), (13, 2), (14, 2), (15, 2), (16, 2), (17, 2), (18, 2), (19, 2), (20, 2), (21, 2), (22, 2),
(23, 3), (24, 3), (25, 3), (26, 3), (27, 3), (28, 3), (29, 3), (30, 3),
(31, 3), (32, 3), (33, 3), (34, 3), (35, 3), (36, 3), (37, 3), (38, 3), (39, 3), (40, 3), (41, 3), (42, 3)
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 4. ADMINISTRADORES
INSERT INTO admins (id, user_id, department, can_audit) VALUES
(1, 1, 'DIRECCION_GENERAL', TRUE),
(2, 2, 'SUPERVISION_OPERATIVA', TRUE)
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    department = VALUES(department),
    can_audit = VALUES(can_audit);

-- 5. PROVEEDORES
INSERT INTO providers (id, user_id, company_name, tax_id, commercial_registry, commercial_address, city, lat, lng, rating, verified) VALUES
(1, 3, 'Maquinaria Andina SAS', '900.123.456-1', 'REG-CAM-0100', 'Cra 68 # 45-12, Parque Industrial Salitre, Bogotá', 'Bogotá', 4.6534, -74.0836, 4.9, TRUE),
(2, 4, 'Gecolsa Maquinaria Pesada SAS', '900.234.567-2', 'REG-CAM-0200', 'Autopista Medellín Km 7, Parque Industrial Cota', 'Bogotá', 4.7450, -74.0430, 4.8, TRUE),
(3, 5, 'Rentas y Equipos de Antioquia SAS', '900.345.678-3', 'REG-CAM-0300', 'Cra 50 # 10-20, Barrio Guayabal, Medellín', 'Medellín', 6.2442, -75.5812, 4.7, TRUE),
(4, 6, 'ConstruAlquiler Caribe SAS', '900.456.789-4', 'REG-CAM-0400', 'Vía 40 # 73-290, Zona Industrial, Barranquilla', 'Barranquilla', 10.9685, -74.7813, 5.0, TRUE),
(5, 7, 'Maquinaria y Andamios del Valle Ltda', '900.567.890-5', 'REG-CAM00', 'Calle 15 # 28-30, Zona Industrial Acopi, Cali', 'Cali', 3.4516, -76.5320, 4.6, TRUE),
(6, 8, 'Herramientas Industriales del Oriente', '900.678.901-6', 'REG-CAM-0600', 'Cra 27 # 36-15, Barrio Bolívar, Bucaramanga', 'Bucaramanga', 7.1254, -73.1198, 4.8, TRUE),
(7, 9, 'Sunbelt Rentals Colombia SAS', '900.789.012-7', 'REG-CAM-0700', 'Av Boyacá # 64-20, Normandía, Bogotá', 'Bogotá', 4.6923, -74.0921, 4.9, TRUE),
(8, 10, 'Andamios & Equipos Eje Cafetero', '900.890.123-8', 'REG-CAM-0800', 'Av Santander # 45-10, Manizales', 'Manizales', 5.0689, -75.5174, 4.5, TRUE),
(9, 11, 'Tolima Maquinaria & Construcción', '900.901.234-9', 'REG-CAM-0900', 'Calle 60 # 12-18, Zona Industrial Mirolindo, Ibagué', 'Ibagué', 4.4389, -75.2322, 4.7, TRUE),
(10, 12, 'Rentas Surcolombiana Huila', '901.012.345-0', 'REG-CAM-1000', 'Cra 5 # 10-30, Zona Comercial, Neiva', 'Neiva', 2.9273, -75.2819, 4.6, TRUE),
(11, 13, 'RentaMaq Altiplano Boyacá', '901.123.456-1', 'REG-CAM-1100', 'Calle 20 # 9-45, Centro Empresarial, Tunja', 'Tunja', 5.5353, -73.3678, 4.8, TRUE),
(12, 14, 'Equipos y Logística del Llano', '901.234.567-2', 'REG-CAM-1200', 'Cra 40 # 25-10, Anillo Vial, Villavicencio', 'Villavicencio', 4.1420, -73.6266, 4.9, TRUE),
(13, 15, 'Suministros Mineros & Obras Envigado', '901.345.678-3', 'REG-CAM-1300', 'Av Las Vegas # 18-50, Envigado', 'Envigado', 6.1759, -75.5917, 5.0, TRUE),
(14, 16, 'Equipos del Sinú Montería', '901.456.789-4', 'REG-CAM-1400', 'Calle 29 # 4-20, Barrio Chuchurubí, Montería', 'Montería', 8.7479, -75.8814, 4.4, TRUE),
(15, 17, 'Alquileres Cesar & Guajira SAS', '901.567.890-5', 'REG-CAM-1500', 'Cra 9 # 15-30, Salida a La Paz, Valledupar', 'Valledupar', 10.4631, -73.2532, 4.7, TRUE),
(16, 18, 'Maquinaria del Café Armenia', '901.678.901-6', 'REG-CAM-1600', 'Cra 14 # 21-15, Armenia', 'Armenia', 4.5339, -75.6811, 4.8, TRUE),
(17, 19, 'Rentas Civiles Pereira SAS', '901.789.012-7', 'REG-CAM-1700', 'Av 30 de Agosto # 40-10, Pereira', 'Pereira', 4.8133, -75.6961, 4.9, TRUE),
(18, 20, 'Equipos Industriales Cúcuta', '901.890.123-8', 'REG-CAM-1800', 'Av Gran Colombia # 10-25, Cúcuta', 'Cúcuta', 7.8939, -72.5078, 4.6, TRUE),
(19, 21, 'ConstruSoluciones Sabana Norte', '901.901.234-9', 'REG-CAM-1900', 'Autopista Norte Km 21, Chía / Cundinamarca', 'Chía', 4.8622, -74.0583, 4.8, TRUE),
(20, 22, 'HeavyEquipment Bogotá SAS', '902.012.345-0', 'REG-CAM-2000', 'Calle 13 # 86-40, Zona Industrial Fontibón, Bogotá', 'Bogotá', 4.6582, -74.1354, 5.0, TRUE)
ON DUPLICATE KEY UPDATE
    company_name = VALUES(company_name),
    tax_id = VALUES(tax_id),
    commercial_registry = VALUES(commercial_registry),
    commercial_address = VALUES(commercial_address),
    city = VALUES(city),
    lat = VALUES(lat),
    lng = VALUES(lng),
    rating = VALUES(rating),
    verified = VALUES(verified);

-- 6. CLIENTES
INSERT INTO clients (id, user_id, document_type, document_number, address, city, preferred_payment_method, lat, lng) VALUES
(1, 23, 'NIT', '901.111.222-1', 'Cra 15 # 85-30, Chico Norte, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6712, -74.0589),
(2, 24, 'NIT', '901.222.333-2', 'Calle 90 # 19C-32, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6818, -74.0536),
(3, 25, 'NIT', '901.333.444-3', 'Cra 27 # 36-10, Bucaramanga', 'Bucaramanga', 'TARJETA_CREDITO', 7.1212, -73.1189),
(4, 26, 'NIT', '901.444.555-4', 'Calle 10Sur # 50FF-28, El Poblado, Medellín', 'Medellín', 'TARJETA_CREDITO', 6.2081, -75.5712),
(5, 27, 'NIT', '901.555.666-5', 'Av 4N # 20-45, Versalles, Cali', 'Cali', 'TARJETA_CREDITO', 3.4612, -76.5289),
(6, 28, 'CC', '1.020.304.050', 'Calle 127 # 45-10, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.7123, -74.0532),
(7, 29, 'CC', '1.032.456.789', 'Cra 7 # 116-50, Usaquén, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6989, -74.0321),
(8, 30, 'NIT', '901.777.888-7', 'Calle 100 # 19-45, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6882, -74.0489),
(9, 31, 'NIT', '901.888.999-8', 'Cra 43A # 1-50, Milla de Oro, Medellín', 'Medellín', 'TARJETA_CREDITO', 6.2012, -75.5789),
(10, 32, 'NIT', '901.999.000-9', 'Av Centenario # 15-20, Armenia', 'Armenia', 'TARJETA_CREDITO', 4.5412, -75.6689),
(11, 33, 'NIT', '902.111.222-1', 'Calle 82 # 51-20, Barranquilla', 'Barranquilla', 'TARJETA_CREDITO', 11.0089, -74.8189),
(12, 34, 'NIT', '902.222.333-2', 'Av Caracas con Calle 26, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6212, -74.0732),
(13, 35, 'NIT', '902.333.444-3', 'Cra 7 # 24-89, Torre Colpatria, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6112, -74.0689),
(14, 36, 'NIT', '902.444.555-4', 'Calle 15 # 4-50, Cali', 'Cali', 'TARJETA_CREDITO', 3.4412, -75.2289),
(15, 37, 'CC', '1.050.607.080', 'Cra 8 # 22-15, Tunja', 'Tunja', 'TARJETA_CREDITO', 5.5389, -73.3589),
(16, 38, 'NIT', '902.555.666-5', 'Av Circunvalar # 18-30, Villavicencio', 'Villavicencio', 'TARJETA_CREDITO', 4.1489, -73.6189),
(17, 39, 'NIT', '902.666.777-6', 'Cra 40 # 25-10, Villavicencio', 'Villavicencio', 'TARJETA_CREDITO', 4.1420, -73.6289),
(18, 40, 'CC', '1.070.809.010', 'Calle 80 # 68-15, Bogotá', 'Bogotá', 'TARJETA_CREDITO', 4.6889, -74.0889),
(19, 41, 'NIT', '902.777.888-7', 'Cra 1 # 10-20, Bocagrande, Cartagena', 'Cartagena', 'TARJETA_CREDITO', 10.4212, -75.5489),
(20, 42, 'NIT', '902.888.999-8', 'Calle 72 # 54-10, Barranquilla', 'Barranquilla', 'TARJETA_CREDITO', 10.9981, -74.8012)
ON DUPLICATE KEY UPDATE
    document_type = VALUES(document_type),
    document_number = VALUES(document_number),
    address = VALUES(address),
    city = VALUES(city),
    preferred_payment_method = VALUES(preferred_payment_method),
    lat = VALUES(lat),
    lng = VALUES(lng);

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
ON DUPLICATE KEY UPDATE id=id;

-- 8. HERRAMIENTAS
INSERT INTO tools (id, provider_id, category_id, code, name, brand, model, daily_price, deposit_amount, status, accumulated_rental_days, lat, lng) VALUES
(1, 1, 1, 'CAT-320D', 'Retroexcavadora Oruga CAT 320D', 'Caterpillar', '320D', 850000, 2500000, 'DISPONIBLE', 15, 4.6534, -74.0836),
(2, 2, 1, 'BOB-S570', 'Minicargador Bobcat S570', 'Bobcat', 'S570', 350000, 1200000, 'DISPONIBLE', 8, 4.7450, -74.0430),
(3, 3, 4, 'BOS-GSH27', 'Martillo Demoledor Eléctrico 30kg', 'Bosch', 'GSH 27 VC', 95000, 300000, 'DISPONIBLE', 4, 6.2442, -75.5812),
(4, 4, 2, 'WAC-CT36', 'Allanadora de Concreto Helicóptero', 'Wacker Neuson', 'CT 36-5A', 120000, 400000, 'DISPONIBLE', 6, 10.9685, -74.7813),
(5, 5, 7, 'MIK-MTX70', 'Bailarina Compactadora Canguro', 'Mikasa', 'MTX70E', 80000, 250000, 'DISPONIBLE', 12, 3.4516, -76.5320),
(6, 6, 5, 'CUM-C55D5', 'Planta Eléctrica Diésel 50kVA Insonorizada', 'Cummins', 'C55D5', 280000, 1000000, 'DISPONIBLE', 20, 7.1254, -73.1198),
(7, 7, 3, 'AND-CERT-01', 'Torre de Andamio Multidireccional 6m', 'Layher', 'Allround', 75000, 200000, 'DISPONIBLE', 30, 4.6923, -74.0921),
(8, 8, 6, 'HUS-FS400', 'Cortadora de Pavimento Disco 14"', 'Husqvarna', 'FS 400 LV', 110000, 350000, 'DISPONIBLE', 9, 5.0689, -75.5174),
(9, 9, 8, 'HON-WT30X', 'Motobomba de Lodos y Sólidos 3"', 'Honda', 'WT30XK4', 85000, 250000, 'DISPONIBLE', 5, 4.4389, -75.2322),
(10, 10, 2, 'KOL-1.5B', 'Mezcladora de Concreto 1.5 Bultos a Gasolina', 'Kollser', 'M150', 65000, 200000, 'DISPONIBLE', 22, 2.9273, -75.2819),
(11, 11, 12, 'GEN-RL4', 'Torre de Iluminación Diésel 4x1000W', 'Generac', 'MLT3060', 160000, 600000, 'DISPONIBLE', 14, 5.5353, -73.3678),
(12, 12, 9, 'SUL-185', 'Compresor de Aire Portátil Diésel 185 CFM', 'Sullair', '185 Tier 4', 220000, 800000, 'DISPONIBLE', 18, 4.1420, -73.6266),
(13, 13, 7, 'DYN-CC1200', 'Rodillo Vibrocompactador Doble 1.2 Ton', 'Dynapac', 'CC1200', 310000, 1100000, 'DISPONIBLE', 10, 6.1759, -75.5917),
(14, 14, 13, 'LEI-TS07', 'Estación Total Topográfica Láser', 'Leica Geosystems', 'FlexLine TS07', 190000, 700000, 'DISPONIBLE', 7, 8.7479, -75.8814),
(15, 15, 10, 'GEN-GS1930', 'Plataforma Elevadora Tijera Eléctrica 8m', 'Genie', 'GS-1930', 180000, 650000, 'DISPONIBLE', 16, 10.4631, -73.2532),
(16, 16, 4, 'HIL-TE3000', 'Martillo Rompedor Pesado AVR 1800W', 'Hilti', 'TE 3000-AVR', 130000, 450000, 'DISPONIBLE', 11, 4.5339, -75.6811),
(17, 17, 11, 'MIL-BOBCAT', 'Motosoldadora Diésel Multiproceso 250A', 'Miller', 'Bobcat 250', 175000, 600000, 'DISPONIBLE', 13, 4.8133, -75.6961),
(18, 18, 15, 'KAR-HD10', 'Hidrolavadora Industrial Alta Presión 3000 PSI', 'Kärcher', 'HD 10/25-4 S', 90000, 300000, 'DISPONIBLE', 9, 7.8939, -72.5078),
(19, 19, 2, 'ENR-RUL', 'Regla Vibratoria de Concreto Telescópica 3m', 'Enar', 'Huracán H', 70000, 220000, 'DISPONIBLE', 6, 4.8622, -74.0583),
(20, 20, 1, 'JCB-3CX', 'Retroexcavadora Mixta JCB 3CX 4x4', 'JCB', '3CX Eco', 720000, 2200000, 'DISPONIBLE', 25, 4.6582, -74.1354),
(21, 1, 6, 'HIL-DD250', 'Perforadora Sacanúcleos Diamante Diam 300mm', 'Hilti', 'DD 250', 140000, 500000, 'DISPONIBLE', 8, 4.6534, -74.0836),
(22, 2, 14, 'GRA-ULTRA', 'Equipo de Pintura Airless Graco UltraMax', 'Graco', 'Ultra Max II 695', 115000, 400000, 'DISPONIBLE', 5, 4.7450, -74.0430),
(23, 3, 10, 'MAL-500KG', 'Malacate Eléctrico de Obra 500kg con Brazo', 'Imer', 'Airone 200N', 60000, 180000, 'DISPONIBLE', 14, 6.2442, -75.5812),
(24, 4, 2, 'VIB-CONC-5M', 'Vibrador de Concreto a Gasolina Manguera 5m', 'Wacker Neuson', 'M2500', 45000, 150000, 'DISPONIBLE', 19, 10.9685, -74.7813),
(25, 5, 7, 'RAN-COMP-90', 'Placa Compactadora Reversible 90kg', 'Mikasa', 'MVC-F80R', 85000, 260000, 'DISPONIBLE', 11, 3.4516, -76.5320)
ON DUPLICATE KEY UPDATE id=id;

-- 9. FOTOS DE HERRAMIENTAS
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
ON DUPLICATE KEY UPDATE id=id;

-- 10. ESPECIFICACIONES TÉCNICAS
INSERT INTO tool_specifications (id, tool_id, spec_name, spec_value) VALUES
(1, 1, 'Potencia / Peso', 'Motor Cat C7.1 ACERT 148 HP, Peso Operativo 22.100 kg, Profundidad de excavación 6.72 m'),
(2, 2, 'Capacidad Operativa', 'Carga Nominal 885 kg, Motor Diésel Bobcat 2.4L 61 HP, Altura de elevación 3.02 m'),
(3, 3, 'Energía de Impacto', 'Fuerza 62 Joules, Potencia 2000 Watts, Tasa de impacto 1000 bpm, Peso 29.5 kg'),
(4, 4, 'Diámetro de Acabado', 'Rotor 915 mm (36 pulgadas), Motor Honda GX160 5.5 HP a gasolina, 4 aspas ajustables'),
(5, 5, 'Fuerza de Compactación', 'Impacto 14.9 kN (1520 kgf), Motor Honda GXR120 4T 3.5 HP, Zapata 285x340 mm'),
(6, 6, 'Potencia Standby', '55 kVA / 44 kW trifásico 220/127V, Motor Cummins 4BT3.3G5, Cabina insonorizada 68 dBA'),
(7, 7, 'Carga Certificada', 'Acero HDG galvanizado en caliente, Capacidad 600 kg/m² (Clase 6 EN-12810), Módulos 2.57m'),
(8, 8, 'Profundidad de Corte', 'Disco diamantado 350-450 mm, Corte máx 162 mm, Motor Honda GX390 11.7 HP c/tanque 30L'),
(9, 9, 'Caudal y Paso de Sólidos', 'Descarga 1210 L/min (72 m³/h), Paso de sólidos hasta 28 mm, Motor Honda GX240 7.9 HP'),
(10, 10, 'Capacidad Tambor', 'Volumen 380 Litros (1.5 bultos), Motor Kohler Command PRO 9.5 HP, Volteo reforzado'),
(11, 11, 'Área de Cobertura', 'Mástil 9 metros rotación 360°, 4 lámparas 1000W (440.000 lúmenes), Generador 6 kW'),
(12, 12, 'Flujo de Aire Comprimido', '185 CFM (5.2 m³/min) a 100 PSI (6.9 bar), Motor Kubota Tier 4 diésel, 2 salidas 3/4"'),
(13, 13, 'Frecuencia y Vibración', 'Ancho de rollo 1200 mm, Carga lineal 11 kg/cm, Frecuencia 58 Hz, Fuerza centrífuga 27 kN'),
(14, 14, 'Precisión Topográfica', 'Angular 2 segundos, Alcance EDM sin prisma 500 m / con prisma 3500 m, Memoria USB'),
(15, 15, 'Altura de Trabajo', 'Plataforma tijera 7.79 m, Capacidad 227 kg (2 operarios), Baterías ciclo profundo 24V'),
(16, 16, 'Rendimiento Rompedor', 'Energía 68 Joules, Tasa 860 gpm, Reducción de vibración AVR <7 m/s², Mandril hexagonal 28mm'),
(17, 17, 'Rango de Soldadura', 'Salida CC/CV 250A multiproceso (Electrodo, MIG, TIG), Generador trifásico 11.000 Watts'),
(18, 18, 'Presión Hidráulica', '250 bar (3625 PSI), Caudal 1000 L/h, Motor 4 polos trifásico 7.8 kW refrigerado por agua'),
(19, 19, 'Perfil Extruido', 'Regla 3.0 metros aluminio magnesio, Motor Honda GX35 4T 1.3 HP, Desencofrado rápido'),
(20, 20, 'Versatilidad 4x4', 'Motor JCB EcoMAX 74 HP turbo, Tracción 4WD, Balde 1.0 m³, Brazo excavador alcance 5.46 m'),
(21, 21, 'Diámetro de Perforación', 'Corona diamantada 12 a 300 mm, Motor 3200 Watts 4 velocidades, Anclaje en concreto'),
(22, 22, 'Caudal de Aspersión', 'Rendimiento 3.0 L/min, Presión 3300 PSI, Motor MaxPower sin escobillas 2.0 HP'),
(23, 23, 'Carga de Izaje', 'Capacidad 500 kg, Cable acero antigiro 30 m, Motor freno 220V 1.5 kW, Brazo extensible'),
(24, 24, 'Frecuencia de Aguja', 'Diámetro 45 mm, Eje flexible 5 metros, 12.000 vpm, Motor Wacker Neuson 5.5 HP gasolina'),
(25, 25, 'Compactación Reversible', 'Fuerza 19.8 kN (2020 kgf), Avance 25 m/min, Base 500x350 mm, Motor Robin Subaru 5.7 HP')
ON DUPLICATE KEY UPDATE id=id;

-- 11. RESERVAS Y ALQUILERES
INSERT INTO rentals (id, client_id, tool_id, start_date, end_date, total_days, daily_rate, deposit_amount, subtotal, status, delivery_address) VALUES
(1, 1, 1, '2026-09-03', '2026-09-07', 4, 850000.0, 2500000.0, 3400000.0, 'COMPLETADA', 'Proyecto Torre Cusezar 93 - Cra 11 # 93-45, Bogotá'),
(2, 2, 2, '2026-09', '2026-09-10', 5, 350000.0, 1200000.0, 1750000.0, 'COMPLETADA', 'Construcción Viaducto Ruta del Sol - Sector Villeta'),
(3, 3, 3, '2026-09-07', '2026-09-13', 6, 95000.0, 300000.0, 570000.0, 'COMPLETADA', 'Complejo Residencial Jardines de Suba - Cra 104 # 148-20, Bogotá'),
(4, 4, 4, '2026-09-09', '2026-09-16', 7, 120000.0, 400000.0, 840000.0, 'COMPLETADA', 'Edificio Corporativo Calle 72 - Calle 72 # 10-34, Bogotá'),
(5, 5, 5, '2026-09-11', '2026-09-14', 3, 80000.0, 250000.0, 240000.0, 'COMPLETADA', 'Planta Industrial Tocancipá - Parque Industrial El Olympo'),
(6, 6, 6, '2026-09-13', '2026-09-17', 4, 280000.0, 1000000.0, 1120000.0, 'COMPLETADA', 'Parque Logístico Siberia - Bodega 14, Autopista Medellín Km 3'),
(7, 7, 7, '2026-09-15', '2026-09-20', 5, 75000.0, 200000.0, 375000.0, 'COMPLETADA', 'Edificio Milla de Oro - Cra 43A # 1-50, El Poblado, Medellín'),
(8, 8, 8, '2026-09-17', '2026-09-23', 6, 110000.0, 350000.0, 660000.0, 'COMPLETADA', 'Ciudadela Universitaria del Valle - Calle 13 # 100-00, Cali'),
(9, 9, 9, '2026-09-19', '2026-09-26', 7, 85000.0, 250000.0, 595000.0, 'COMPLETADA', 'Construcción Torre Malecón del Río - Vía 40 con Calle 78, Barranquilla'),
(10, 10, 10, '2026-09-21', '2026-09-24', 3, 65000.0, 200000.0, 195000.0, 'COMPLETADA', 'Pavimentación Anillo Vial Oriental - Floridablanca / Bucaramanga'),
(11, 11, 11, '2026-09-23', '2026-09-27', 4, 160000.0, 600000.0, 640000.0, 'COMPLETADA', 'Condominio Campestre La Sabana - Km 4 Vía Chía-Cajicá'),
(12, 12, 12, '2026-09-25', '2026-09-30', 5, 220000.0, 800000.0, 1100000.0, 'COMPLETADA', 'Puente Peatonal Carrera 68 con Calle 26 - Bogotá D.C.'),
(13, 13, 13, '2026-09-27', '2026-10-03', 6, 310000.0, 1100000.0, 1860000.0, 'COMPLETADA', 'Remodelación Centro Comercial Andino - Cra 11 # 82-71, Bogotá'),
(14, 14, 14, '2026-09-29', '2026-10-06', 7, 190000.0, 700000.0, 1330000.0, 'COMPLETADA', 'Construcción Túnel del Toyo - Cañasgordas / Antioquia'),
(15, 15, 15, '2026-10-01', '2026-10-04', 3, 180000.0, 650000.0, 540000.0, 'COMPLETADA', 'Edificio Altos de la Flora - Av 6 Norte # 38N-12, Cali'),
(16, 16, 16, '2026-10-03', '2026-10-07', 4, 130000.0, 450000.0, 520000.0, 'CONFIRMADA', 'Viaducto Gran Manglar - Km 12 Vía al Mar, Cartagena'),
(17, 17, 17, '2026-10', '2026-10-10', 5, 175000.0, 600000.0, 875000.0, 'CONFIRMADA', 'Parque Eólico La Guajira - Sector Uribia'),
(18, 18, 18, '2026-10-07', '2026-10-13', 6, 90000.0, 300000.0, 540000.0, 'CONFIRMADA', 'Centro Logístico Funza - Parque Industrial Galicia'),
(19, 19, 19, '2026-10-09', '2026-10-16', 7, 70000.0, 220000.0, 490000.0, 'CONFIRMADA', 'Urbanización Hacienda Santa Fe - Chía / Cundinamarca'),
(20, 20, 20, '2026-10-11', '2026-10-14', 3, 720000.0, 2200000.0, 2160000.0, 'CONFIRMADA', 'Doble Calzada Bogotá-Villavicencio - Kilómetro 58'),
(21, 1, 21, '2026-10-13', '2026-10-17', 4, 140000.0, 500000.0, 560000.0, 'PENDIENTE', 'Edificio Residencial Santa Bárbara - Calle 119 # 14-30, Bogotá'),
(22, 2, 22, '2026-10-15', '2026-10-20', 5, 115000.0, 400000.0, 575000.0, 'PENDIENTE', 'Clínica Imbanaco Sede Norte - Cra 38 # 5B-10, Cali'),
(23, 3, 23, '2026-10-17', '2026-10-23', 6, 60000.0, 180000.0, 360000.0, 'PENDIENTE', 'Estación Metro Calle 72 con Caracas - Bogotá'),
(24, 4, 24, '2026-10-19', '2026-10-26', 7, 45000.0, 150000.0, 315000.0, 'PENDIENTE', 'Portal Norte TransMilenio Ampliación - Autopista Norte con 170'),
(25, 5, 25, '2026-10-21', '2026-10-24', 3, 85000.0, 260000.0, 255000.0, 'PENDIENTE', 'Complejo Portuario Sociedad Portuaria - Manga, Cartagena')
ON DUPLICATE KEY UPDATE id=id;

-- 12. DEVOLUCIÓN E INSPECCIÓN FÍSICA
INSERT INTO rental_returns (id, rental_id, inspector_user_id, equipment_condition, has_damage, deposit_refunded, notes) VALUES
(1, 1, 1, 'EXCELENTE', FALSE, 2500000.00, 'Inspección técnica completada. Equipo limpio y calibrado.'),
(2, 2, 1, 'EXCELENTE', FALSE, 1200000.00, 'Entrega en patio conforme a protocolo. Niveles de fluidos en orden.'),
(3, 3, 1, 'EXCELENTE', FALSE, 300000.00, 'Motor eléctrico y carbones en estado óptimo.'),
(4, 4, 1, 'DAÑOS_LEVES', TRUE, 280000.00, 'Aspas de helicóptero con desgaste moderado. Descuento aplicado.'),
(5, 5, 1, 'EXCELENTE', FALSE, 250000.00, 'Fuelle y zapatón en perfecto estado de compresión.'),
(6, 6, 1, 'EXCELENTE', FALSE, 1000000.00, 'Generador entregado con horómetro verificado y tanque lleno.'),
(7, 7, 1, 'EXCELENTE', FALSE, 200000.00, 'Todos los cuerpos de andamio certificados y completos.'),
(8, 8, 1, 'DAÑOS_LEVES', TRUE, 245000.00, 'Protector de disco con fisura por impacto leve.'),
(9, 9, 1, 'EXCELENTE', FALSE, 250000.00, 'Turbina y sello mecánico inspeccionados sin obstrucciones.'),
(10, 10, 1, 'EXCELENTE', FALSE, 200000.00, 'Tambor libre de incrustaciones de concreto.'),
(11, 11, 1, 'EXCELENTE', FALSE, 600000.00, 'Mástil telescópico y reflectores 1000W operativos.'),
(12, 12, 1, 'DAÑOS_LEVES', TRUE, 560000.00, 'Filtro de aire saturado y válvula de purga sucia.'),
(13, 13, 1, 'EXCELENTE', FALSE, 1100000.00, 'Tambores vibratorios y sistema de aspersión en regla.'),
(14, 14, 1, 'EXCELENTE', FALSE, 700000.00, 'Calibración óptica y prismas verificados con estación base.'),
(15, 15, 1, 'EXCELENTE', FALSE, 650000.00, 'Sistema hidráulico de elevación tijera y baterías al 100%.'),
(16, 16, 1, 'DAÑOS_LEVES', TRUE, 315000.00, 'Puntero hexagonal con desgaste mayor al esperado.'),
(17, 17, 1, 'EXCELENTE', FALSE, 600000.00, 'Cables de masa y porta-electrodo sin deterioro.'),
(18, 18, 1, 'EXCELENTE', FALSE, 300000.00, 'Bomba cerámica de alta presión y lanza rotativa en perfecto estado.'),
(19, 19, 1, 'EXCELENTE', FALSE, 220000.00, 'Perfil de aluminio recto sin deformaciones.'),
(20, 20, 1, 'DAÑOS_LEVES', TRUE, 1540000.00, 'Manguera hidráulica secundaria con roce de zanja.')
ON DUPLICATE KEY UPDATE id=id;

-- 13. REPORTES DE DAÑOS Y PENALIZACIONES
INSERT INTO return_damages (id, rental_return_id, description, penalty_fee) VALUES
(1, 1, 'Desgaste operacional normal en tren de rodaje de orugas', 0.00),
(2, 2, 'Limpieza técnica profunda por polvo de excavación', 0.00),
(3, 3, 'Revisión preventiva de escobillas de motor', 0.00),
(4, 4, 'Reemplazo de juego de aspas de acabado 36"', 120000.00),
(5, 5, 'Inspección de aceite de pata de impacto', 0.00),
(6, 6, 'Reemplazo de filtro de combustible diésel primario', 0.00),
(7, 7, 'Revisión de pasadores y diagonales de seguridad', 0.00),
(8, 8, 'Reparación de guarda metálica de disco de corte', 105000.00),
(9, 9, 'Lavado de manguera de succión anillada de 3"', 0.00),
(10, 10, 'Alineación de cremallera de volteo de trompo', 0.00),
(11, 11, 'Prueba de fotocelda y cableado de mástil', 0.00),
(12, 12, 'Sustitución de elemento filtrante separador de aceite', 240000.00),
(13, 13, 'Ajuste de rascadores de tambor de rodillo', 0.00),
(14, 14, 'Limpieza de lentes de objetivo y colimador láser', 0.00),
(15, 15, 'Engrase de tijeras y verificación de sensores de inclinación', 0.00),
(16, 16, 'Sustitución de puntero cincel autoafilable TE-SPX', 135000.00),
(17, 17, 'Calibración de reóstato de amperaje de motosoldadora', 0.00),
(18, 18, 'Descalcificación de serpentín y boquilla abanico', 0.00),
(19, 19, 'Ajuste de acople rápido de regla vibratoria', 0.00),
(20, 20, 'Cambio preventivo de manguera hidráulica de retroextensible', 660000.00)
ON DUPLICATE KEY UPDATE id=id;

-- 14. FACTURAS ELECTRÓNICAS
INSERT INTO invoices (id, invoice_number, rental_id, client_id, client_name, client_tax_id, subtotal, tax_amount, deposit_amount, total_amount, paid, pdf_storage_path) VALUES
(1, 'FE-2026-00001', 1, 1, 'Constructora Hábitat SAS', '901.111.222-1', 3400000.00, 646000.00, 2500000.00, 6546000.00, TRUE, '/invoices/FE-2026-00001.pdf'),
(2, 'FE-2026-00002', 2, 2, 'Amarilo Construcciones SAS', '901.222.333-2', 1750000.00, 332500.00, 1200000.00, 3282500.00, TRUE, '/invoices/FE-2026-00002.pdf'),
(3, 'FE-2026-00003', 3, 3, 'Marval Obras y Proyectos SAS', '901.333.444-3', 570000.00, 108300.00, 300000.00, 978300.00, TRUE, '/invoices/FE-2026-00003.pdf'),
(4, 'FE-2026-00004', 4, 4, 'Arquitectura & Concreto SAS', '901.444.555-4', 840000.00, 159600.00, 400000.00, 1399600.00, TRUE, '/invoices/FE-2026-00004.pdf'),
(5, 'FE-2026-00005', 5, 5, 'Consorcio Vial Pacífico', '901.555.666-5', 240000.00, 45600.00, 250000.00, 535600.00, TRUE, '/invoices/FE-2026-00005.pdf'),
(6, 'FE-2026-00006', 6, 6, 'Carlos Mario Restrepo (Contratista Civil)', '1.020.304.050', 1120000.00, 212800.00, 1000000.00, 2332800.00, TRUE, '/invoices/FE-2026-00006.pdf'),
(7, 'FE-2026-00007', 7, 7, 'Mariana Gómez Torres (Ing. Residente)', '1.032.456.789', 375000.00, 71250.00, 200000.00, 646250.00, TRUE, '/invoices/FE-2026-00007.pdf'),
(8, 'FE-2026-00008', 8, 8, 'Cusezar Ingeniería & Construcción', '901.777.888-7', 660000.00, 125400.00, 350000.00, 1135400.00, TRUE, '/invoices/FE-2026-00008.pdf'),
(9, 'FE-2026-00009', 9, 9, 'Conconcreto Infraestructura SAS', '901.888.999-8', 595000.00, 113050.00, 250000.00, 958050.00, TRUE, '/invoices/FE-2026-00009.pdf'),
(10, 'FE-2026-00010', 10, 10, 'Pavimentos Colombia SAS', '901.999.000-9', 195000.00, 37050.00, 200000.00, 432050.00, TRUE, '/invoices/FE-2026-00010.pdf'),
(11, 'FE-2026-00011', 11, 11, 'Prodesa Desarrollos Inmobiliarios', '902.111.222-1', 640000.00, 121600.00, 600000.00, 1361600.00, TRUE, '/invoices/FE-2026-00011.pdf'),
(12, 'FE-2026-00012', 12, 12, 'Consorcio Metro Línea 1', '902.222.333-2', 1100000.00, 209000.00, 800000.00, 2109000.00, TRUE, '/invoices/FE-2026-00012.pdf'),
(13, 'FE-2026-00013', 13, 13, 'Constructora Colpatria SAS', '902.333.444-3', 1860000.00, 353400.00, 1100000.00, 3313400.00, TRUE, '/invoices/FE-2026-00013.pdf'),
(14, 'FE-2026-00014', 14, 14, 'Jaramillo Mora Constructora', '902.444.555-4', 1330000.00, 252700.00, 700000.00, 2282700.00, TRUE, '/invoices/FE-2026-00014.pdf'),
(15, 'FE-2026-00015', 15, 15, 'David Fernando Vega (Maestro de Obra)', '1.050.607.080', 540000.00, 102600.00, 650000.00, 1292600.00, TRUE, '/invoices/FE-2026-00015.pdf'),
(16, 'FE-2026-00016', 16, 16, 'Urbana de Obras y Minería SAS', '902.555.666-5', 520000.00, 98800.00, 450000.00, 1068800.00, TRUE, '/invoices/FE-2026-00016.pdf'),
(17, 'FE-2026-00017', 17, 17, 'Ingeniería de Vías del Llano SAS', '902.666.777-6', 875000.00, 166250.00, 600000.00, 1641250.00, TRUE, '/invoices/FE-2026-00017.pdf'),
(18, 'FE-2026-00018', 18, 18, 'Gabriel Antonio Rivas (Eléctrico)', '1.070.809.010', 540000.00, 102600.00, 300000.00, 942600.00, TRUE, '/invoices/FE-2026-00018.pdf'),
(19, 'FE-2026-00019', 19, 19, 'Constructora El Cóndor SA', '902.777.888-7', 490000.00, 93100.00, 220000.00, 803100.00, TRUE, '/invoices/FE-2026-00019.pdf'),
(20, 'FE-2026-00020', 20, 20, 'Edificaciones Civiles del Norte', '902.888.999-8', 2160000.00, 410400.00, 2200000.00, 4770400.00, TRUE, '/invoices/FE-2026-00020.pdf'),
(21, 'FE-2026-00021', 21, 1, 'Constructora Hábitat SAS', '901.111.222-1', 560000.00, 106400.00, 500000.00, 1166400.00, TRUE, '/invoices/FE-2026-00021.pdf'),
(22, 'FE-2026-00022', 22, 2, 'Amarilo Construcciones SAS', '901.222.333-2', 575000.00, 109250.00, 400000.00, 1084250.00, TRUE, '/invoices/FE-2026-00022.pdf'),
(23, 'FE-2026-00023', 23, 3, 'Marval Obras y Proyectos SAS', '901.333.444-3', 360000.00, 68400.00, 180000.00, 608400.00, TRUE, '/invoices/FE-2026-00023.pdf'),
(24, 'FE-2026-00024', 24, 4, 'Arquitectura & Concreto SAS', '901.444.555-4', 315000.00, 59850.00, 150000.00, 524850.00, TRUE, '/invoices/FE-2026-00024.pdf'),
(25, 'FE-2026-00025', 25, 5, 'Consorcio Vial Pacífico', '901.555.666-5', 255000.00, 48450.00, 260000.00, 563450.00, TRUE, '/invoices/FE-2026-00025.pdf')
ON DUPLICATE KEY UPDATE id=id;

-- 15. ÍTEMS DE FACTURA
INSERT INTO invoice_items (id, invoice_id, tool_id, tool_name, daily_rate, days, subtotal) VALUES
(1, 1, 1, 'Retroexcavadora Oruga CAT 320D', 850000.0, 4, 3400000.0),
(2, 2, 2, 'Minicargador Bobcat S570', 350000.0, 5, 1750000.0),
(3, 3, 3, 'Martillo Demoledor Eléctrico 30kg', 95000.0, 6, 570000.0),
(4, 4, 4, 'Allanadora de Concreto Helicóptero', 120000.0, 7, 840000.0),
(5, 5, 5, 'Bailarina Compactadora Canguro', 80000.0, 3, 240000.0),
(6, 6, 6, 'Planta Eléctrica Diésel 50kVA Insonorizada', 280000.0, 4, 1120000.0),
(7, 7, 7, 'Torre de Andamio Multidireccional 6m', 75000.0, 5, 375000.0),
(8, 8, 8, 'Cortadora de Pavimento Disco 14"', 110000.0, 6, 660000.0),
(9, 9, 9, 'Motobomba de Lodos y Sólidos 3"', 85000.0, 7, 595000.0),
(10, 10, 10, 'Mezcladora de Concreto 1.5 Bultos a Gasolina', 65000.0, 3, 195000.0),
(11, 11, 11, 'Torre de Iluminación Diésel 4x1000W', 160000.0, 4, 640000.0),
(12, 12, 12, 'Compresor de Aire Portátil Diésel 185 CFM', 220000.0, 5, 1100000.0),
(13, 13, 13, 'Rodillo Vibrocompactador Doble 1.2 Ton', 310000.0, 6, 1860000.0),
(14, 14, 14, 'Estación Total Topográfica Láser', 190000.0, 7, 1330000.0),
(15, 15, 15, 'Plataforma Elevadora Tijera Eléctrica 8m', 180000.0, 3, 540000.0),
(16, 16, 16, 'Martillo Rompedor Pesado AVR 1800W', 130000.0, 4, 520000.0),
(17, 17, 17, 'Motosoldadora Diésel Multiproceso 250A', 175000.0, 5, 875000.0),
(18, 18, 18, 'Hidrolavadora Industrial Alta Presión 3000 PSI', 90000.0, 6, 540000.0),
(19, 19, 19, 'Regla Vibratoria de Concreto Telescópica 3m', 70000.0, 7, 490000.0),
(20, 20, 20, 'Retroexcavadora Mixta JCB 3CX 4x4', 720000.0, 3, 2160000.0),
(21, 21, 21, 'Perforadora Sacanúcleos Diamante Diam 300mm', 140000.0, 4, 560000.0),
(22, 22, 22, 'Equipo de Pintura Airless Graco UltraMax', 115000.0, 5, 575000.0),
(23, 23, 23, 'Malacate Eléctrico de Obra 500kg con Brazo', 60000.0, 6, 360000.0),
(24, 24, 24, 'Vibrador de Concreto a Gasolina Manguera 5m', 45000.0, 7, 315000.0),
(25, 25, 25, 'Placa Compactadora Reversible 90kg', 85000.0, 3, 255000.0)
ON DUPLICATE KEY UPDATE id=id;

-- 16. PAGOS
INSERT INTO payments (id, invoice_id, rental_id, client_id, amount, payment_method, transaction_reference, status) VALUES
(1, 1, 1, 1, 5900000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1001', 'PAGADO'),
(2, 2, 2, 2, 2950000.00, 'Transferencia Bancaria Bancolombia', 'TRX-2026-1002', 'PAGADO'),
(3, 3, 3, 3, 870000.00, 'Efectivo / Cheque de Gerencia', 'TRX-2026-1003', 'PAGADO'),
(4, 4, 4, 4, 1240000.00, 'PSE Débito Banco de Bogotá', 'TRX-2026-1004', 'PAGADO'),
(5, 5, 5, 5, 490000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1005', 'PAGADO'),
(6, 6, 6, 6, 2120000.00, 'Transferencia Bancaria Davivienda', 'TRX-2026-1006', 'PAGADO'),
(7, 7, 7, 7, 575000.00, 'PSE Débito Bancolombia', 'TRX-2026-1007', 'PAGADO'),
(8, 8, 8, 8, 1010000.00, 'PSE Débito BBVA', 'TRX-2026-1008', 'PAGADO'),
(9, 9, 9, 9, 845000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1009', 'PAGADO'),
(10, 10, 10, 10, 395000.00, 'Transferencia Bancaria Bancolombia', 'TRX-2026-1010', 'PAGADO'),
(11, 11, 11, 11, 1240000.00, 'PSE Débito Banco de Occidente', 'TRX-2026-1011', 'PAGADO'),
(12, 12, 12, 12, 1900000.00, 'PSE Débito Davivienda', 'TRX-2026-1012', 'PAGADO'),
(13, 13, 13, 13, 2960000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1013', 'PAGADO'),
(14, 14, 14, 14, 2030000.00, 'Transferencia Bancaria Bancolombia', 'TRX-2026-1014', 'PAGADO'),
(15, 15, 15, 15, 1190000.00, 'PSE Débito Banco Popular', 'TRX-2026-1015', 'PAGADO'),
(16, 16, 16, 16, 970000.00, 'PSE Débito Bancolombia', 'TRX-2026-1016', 'PAGADO'),
(17, 17, 17, 17, 1475000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1017', 'PAGADO'),
(18, 18, 18, 18, 840000.00, 'Transferencia Bancaria Davivienda', 'TRX-2026-1018', 'PAGADO'),
(19, 19, 19, 19, 710000.00, 'PSE Débito Scotiabank Colpatria', 'TRX-2026-1019', 'PAGADO'),
(20, 20, 20, 20, 4360000.00, 'PSE Débito Bancolombia', 'TRX-2026-1020', 'PAGADO'),
(21, 21, 21, 1, 1060000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1021', 'PAGADO'),
(22, 22, 22, 2, 975000.00, 'Transferencia Bancaria Bancolombia', 'TRX-2026-1022', 'PAGADO'),
(23, 23, 23, 3, 540000.00, 'PSE Débito Davivienda', 'TRX-2026-1023', 'PAGADO'),
(24, 24, 24, 4, 465000.00, 'PSE Débito Banco de Bogotá', 'TRX-2026-1024', 'PAGADO'),
(25, 25, 25, 5, 515000.00, 'Tarjeta de Crédito Corporativa', 'TRX-2026-1025', 'PAGADO')
ON DUPLICATE KEY UPDATE id=id;

-- 17. AUDITORÍA INICIAL DE EVENTOS (TIMELINE EVENT SOURCING)
INSERT INTO rental_audit_logs (id, rental_id, previous_status, new_status, changed_by, reason, created_at) VALUES
(1, 1, NULL, 'PENDIENTE', 'cliente', 'Solicitud inicial de alquiler radicada en plataforma', '2026-09-01 08:30:00'),
(2, 1, 'PENDIENTE', 'CONFIRMADA', 'proveedor', 'Reserva aprobada por Maquinaria Andina SAS', '2026-09-01 10:15:00'),
(3, 1, 'CONFIRMADA', 'EN_CURSO', 'proveedor', 'Retroexcavadora despachada en cama baja a Proyecto Torre Cusezar 93', '2026-09-03 07:00:00'),
(4, 1, 'EN_CURSO', 'COMPLETADA', 'proveedor', 'Devolución recibida en patio. Inspección técnica aprobada sin daños', '2026-09-07 17:30:00'),

(5, 16, NULL, 'PENDIENTE', 'cliente16', 'Solicitud de alquiler de Martillo Rompedor Hilti para Viaducto Gran Manglar', '2026-10-02 14:00:00'),
(6, 16, 'PENDIENTE', 'CONFIRMADA', 'proveedor16', 'Disponibilidad confirmada por Maquinaria del Café Armenia', '2026-10-03 09:20:00'),

(7, 21, NULL, 'PENDIENTE', 'cliente', 'Solicitud radicada por Constructora Hábitat para Perforadora Sacanúcleos', '2026-10-13 11:45:00')
ON DUPLICATE KEY UPDATE id=id;

-- SINCRONIZACIÓN DE SECUENCIAS AUTO-INCREMENTALES
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT
-- Sequence synchronized automatically in MySQL via AUTO_INCREMENT

SET FOREIGN_KEY_CHECKS = 1;
