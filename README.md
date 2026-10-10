# 🏗️ Plataforma de Renta de Herramientas y Equipos de Construcción

> **Proyecto Oficial:** `Proyecto_RentaHerramientas`  
> **Arquitectura:** DDD Hexagonal Multi-módulo + Spring Boot 3.4 / 4.0 (Java 21)  
> **Base de Datos:** PostgreSQL 16 con Normalización estricta **4FN**  
> **Seguridad:** Spring Security 6 con JWT (RBAC)  
> **Frontend:** Bootstrap 5 con estética **Neumorphism + Glassmorphism**, Leaflet.js y SweetAlert2  
> **Tiempo Real:** Spring WebSocket + STOMP (`/ws-rentatools`)  
> **Portabilidad:** Docker Compose y Maven Wrapper (Zero "solo funciona en mi máquina")

---

## 📌 Contexto del Proyecto

Plataforma empresarial tipo marketplace B2B/B2C para la gestión, reserva, alquiler, inspección y facturación de maquinaria pesada y herramientas de construcción (retroexcavadoras, andamios, torres de iluminación, mezcladoras, taladros industriales).

### 👥 Perfiles de Usuario
- **👑 Administrador (`ROLE_ADMIN`):** Gestión de usuarios, métricas de ingresos, supervisión de devoluciones e incidencias de daños.
- **🚜 Proveedor (`ROLE_PROVEEDOR`):** Publicación de inventario con fotos y especificaciones, aceptación de reservas y actas de devolución/daño.
- **👷 Cliente (`ROLE_CLIENTE`):** Búsqueda de maquinaria por cercanía en mapa interactivo, reservas con validación de no solapamiento, pago en línea y descarga de facturas en PDF.

---

## 🧱 Arquitectura Hexagonal (DDD Multi-módulo)

```
Proyecto_RentaHerramientas/
├── domain/            # Entidades puras, Value Objects, Puertos y Excepciones
├── application/       # Casos de uso y DTOs de entrada/salida
├── infrastructure/    # Adaptadores Spring Boot, JPA, Seguridad JWT, WebSockets y PDF
├── frontend/          # Interfaz de usuario responsiva (Neumorphism + Glassmorphism)
└── docs/              # Suite exhaustiva de documentación técnica
```

---

## 🚀 Despliegue Rápido con Docker Compose

Para levantar la base de datos PostgreSQL 16 con todas las tablas en **4FN** y más de 20 semillas cargadas:

```bash
docker compose up -d
```

---

## 📚 Documentación Técnica Detallada

- [**Guía de Contexto para el Desarrollador B / Frontend**](PROMPT_IA_COMPANERO_FRONTEND.md)
- [**Arquitectura DDD y Puertos**](docs/ARCHITECTURE_DDD.md)
- [**Diccionario de Datos y Justificación 4FN**](docs/DATABASE_SETUP_4FN.md)
- [**Seguridad JWT y Roles**](docs/JWT_SECURITY_ROLES.md)
- [**Catálogo de Endpoints Swagger**](docs/SWAGGER_API_CONTRACTS.md)
