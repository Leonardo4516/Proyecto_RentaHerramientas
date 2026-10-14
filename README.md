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

## 🚀 Despliegue y Ejecución Rápida Multi-Plataforma

### 1. Variables de Entorno y Base de Datos (Docker Compose)
Copia el archivo de variables y levanta el contenedor de PostgreSQL 16 con el esquema 4FN y todas las semillas precargadas:

- **Linux / macOS:**
  ```bash
  cp .env.example .env
  docker compose up -d
  ```
- **Windows (CMD / PowerShell):**
  ```cmd
  copy .env.example .env
  docker compose up -d
  ```

---

### 2. Ejecutar la Aplicación Spring Boot
El proyecto utiliza Maven Wrapper, por lo que no requiere tener Maven instalado previamente:

- **Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run -pl infrastructure
  ```
- **Windows (CMD / PowerShell):**
  ```cmd
  mvnw.cmd spring-boot:run -pl infrastructure
  ```

La aplicación quedará disponible en:
- **Frontend Web & Catálogo:** [http://localhost:8080](http://localhost:8080)
- **Documentación Swagger / OpenAPI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **pgAdmin (Opcional):** [http://localhost:8082](http://localhost:8082) (`admin@rentatools.com` / `Admin123456!`)

---

## 📚 Documentación Técnica Detallada

- [**Guía de Contexto para el Desarrollador B / Frontend**](PROMPT_IA_COMPANERO_FRONTEND.md)
- [**Arquitectura DDD y Puertos**](docs/ARCHITECTURE_DDD.md)
- [**Diccionario de Datos y Justificación 4FN**](docs/DATABASE_SETUP_4FN.md)
- [**Seguridad JWT y Roles**](docs/JWT_SECURITY_ROLES.md)
- [**Catálogo de Endpoints Swagger**](docs/SWAGGER_API_CONTRACTS.md)
