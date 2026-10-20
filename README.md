# 🏗️ Plataforma de Renta de Herramientas y Equipos de Construcción (RentaTools)

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4%20%2F%204.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%7C%20MySQL-blue.svg)](https://www.postgresql.org/)
[![Security](https://img.shields.io/badge/Security-Spring%20Security%206%20%2B%20JWT-red.svg)](https://spring.io/projects/spring-security)
[![Architecture](https://img.shields.io/badge/Architecture-DDD%20Hexagonal%204FN-purple.svg)](https://martinfowler.com/bliki/DomainDrivenDesign.html)
[![Packaging](https://img.shields.io/badge/Packaging-WAR%20%28Apache%20Tomcat%29-yellow.svg)](https://tomcat.apache.org/)

Plataforma empresarial tipo marketplace B2B/B2C desarrollada en **Spring Boot** para la gestión, reserva, alquiler, inspección física y facturación electrónica de maquinaria pesada y herramientas de construcción (retroexcavadoras, andamios multidireccionales, plantas eléctricas, torres de iluminación, mezcladoras y martillos demoledores).

---

## 📌 Contexto y Objetivos del Proyecto

El alquiler de maquinaria pesada requiere certidumbre en disponibilidad, verificación de condiciones físicas de entrega y trazabilidad en pagos y garantías. 

### 👥 Perfiles de Usuario y Capacidades (RBAC)
* **👑 Administrador (`ROLE_ADMIN`):**
  * Supervisión global del marketplace y control de flota en mapa nacional.
  * Monitoreo de ingresos totales, tasa de ocupación y usuarios registrados.
  * Trazabilidad de alquileres, devoluciones e incidencias de averías.
* **🚜 Proveedor (`ROLE_PROVEEDOR`):**
  * Gestión de catálogo e inventario (alta de maquinaria con fotos y fichas técnicas).
  * Aprobación o rechazo de solicitudes de alquiler.
  * Actas de recepción, inspección técnica y reportes de penalización por daños.
  * Dashboard de ganancias y ocupación de su flota.
* **👷 Cliente (`ROLE_CLIENTE`):**
  * Búsqueda de maquinaria por categoría y proximidad geoespacial a su obra.
  * Carrito de alquiler multi-equipo persistente en la nube (`Cloud Cart`).
  * Validación atómica de fechas sin solapamiento (*Pessimistic Locking*).
  * Pasarela de pago en línea (PSE / Tarjeta) y descarga de factura oficial en PDF.

---

## 🧱 Arquitectura Hexagonal y Estructura Modular

El proyecto implementa **Domain-Driven Design (DDD)** segregado en 3 módulos Maven:

```text
Proyecto_RentaHerramientas/
├── domain/            # Reglas de negocio puras, Value Objects, Agregados y Puertos (Agnóstico de frameworks)
├── application/       # Casos de uso (UseCases), DTOs y orquestación de negocio
├── infrastructure/    # Adaptadores Spring Boot, JPA, Seguridad JWT, WebSockets, OpenPDF y REST
├── frontend/          # Interfaz web responsiva con Bootstrap 5, Three.js (3D) y Leaflet Maps
└── docs/              # Diagrama relacional, colección Postman y scripts SQL (MySQL y PostgreSQL)
```

---

## 🖼️ Diagrama Relacional de la Base de Datos (4FN)

El diseño de la base de datos se encuentra normalizado en **Cuarta Forma Normal (4FN)** con 18 entidades relacionadas:

![Diagrama Relacional de Base de Datos](docs/diagrama_relacional_bd.png)

> Los scripts DDL y las semillas con **más de 20 registros reales por entidad** se encuentran disponibles en:
> * **PostgreSQL:** [`docs/sql/01_schema_4fn.sql`](docs/sql/01_schema_4fn.sql) y [`docs/sql/02_seeds_20_records.sql`](docs/sql/02_seeds_20_records.sql)
> * **MySQL:** [`docs/sql/01_mysql_schema_4fn.sql`](docs/sql/01_mysql_schema_4fn.sql) y [`docs/sql/02_mysql_seeds_20_records.sql`](docs/sql/02_mysql_seeds_20_records.sql)

---

## 🔑 Credenciales Predeterminadas de Prueba

Para probar de inmediato todos los flujos de la plataforma:

| Rol | Usuario | Contraseña | Perfil / Empresa Representada |
|---|---|---|---|
| **Administrador** | `admin` | `Admin123!` | Carlos Mendoza (Director General RentaTools) |
| **Proveedor** | `proveedor` | `Proveedor123!` | Ing. Alejandro Morales (Maquinaria Andina SAS) |
| **Proveedor 2** | `proveedor2` | `Proveedor123!` | Dr. Gabriel Jaramillo (Gecolsa Maquinaria Pesada SAS) |
| **Cliente** | `cliente` | `Cliente123!` | Ing. Cristian Muñoz (Constructora Hábitat SAS) |
| **Cliente 2** | `cliente2` | `Cliente123!` | Ing. Roberto Camargo (Amarilo Construcciones SAS) |

---

## ⚙️ Configuración del Entorno (`application.yml`)

El backend soporta resolución dinámica de base de datos tanto para **PostgreSQL** como para **MySQL**:

```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    # URL dinámica (compatible con PostgreSQL local, Supabase o MySQL):
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/renta_herramientas_db}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
    driver-class-name: ${SPRING_DATASOURCE_DRIVER_CLASS_NAME:org.postgresql.Driver}

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: ${JWT_SECRET:RentaToolsUltraSecretSigningKeyMustBeAtLeast256BitsLongForHmacSha256Security!}
  expiration-ms: 86400000 # 24 Horas
```

---

## 🚀 Guía de Instalación y Ejecución Multi-Plataforma

### 1. Clonar el Repositorio
```bash
git clone -b develop https://github.com/Leonardo4516/Proyecto_RentaHerramientas.git
cd Proyecto_RentaHerramientas
```

> [!NOTE]
> **Base de Datos en la Nube (Preconfigurada por defecto):**
> El proyecto viene configurado por defecto para conectarse automáticamente a **Supabase (PostgreSQL en la Nube)**. No necesitas instalar ni configurar PostgreSQL, MySQL ni Docker en tu máquina para correr el proyecto. ¡Solo necesitas **Java 17 o 21** e internet!

### 2. Ejecución Inmediata (2 Métodos)

#### 🚀 Método 1: Ejecutar directamente con Maven Wrapper (Recomendado)

Al ser una arquitectura modular (DDD), se ejecuta indicando el módulo de infraestructura (`-pl infrastructure`) o con los scripts directos:

* **En Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run -pl infrastructure
  # O simplemente:
  ./ejecutar.sh
  ```
* **En Windows (CMD o PowerShell):**
  ```cmd
  mvnw.cmd spring-boot:run -pl infrastructure
  rem O simplemente:
  ejecutar.bat
  ```

---

#### 📦 Método 2: Ejecutar mediante el paquete WAR (`rentatools.war`)

El empaquetado del proyecto genera un archivo WAR híbrido que **incluye su propio servidor Apache Tomcat integrado** en su interior.

> [!IMPORTANT]
> **¿El computador debe tener Tomcat instalado?**
> **¡NO!** No necesitas instalar Apache Tomcat por separado. El archivo `.war` contiene su propio servidor web embebido y arranca inmediatamente con el comando `java -jar`.

**1. Generar el archivo WAR:**
```bash
# Linux / macOS:
./mvnw clean package -DskipTests

# Windows:
mvnw.cmd clean package -DskipTests
```
El archivo compilado quedará ubicado exactamente en:
```text
infrastructure/target/rentatools.war
```

**2. ¿Dónde y cómo ejecutar el comando `java -jar`?**

* **Caso A: Si estás parado en la carpeta raíz del proyecto (`Proyecto_RentaHerramientas`):**
  ```bash
  # Linux / macOS:
  java -jar infrastructure/target/rentatools.war

  # Windows (CMD / PowerShell):
  java -jar infrastructure\target\rentatools.war
  ```

* **Caso B: Si abriste la terminal dentro de la carpeta `infrastructure/target/`:**
  ```bash
  cd infrastructure/target
  java -jar rentatools.war
  ```

* **Caso C: Si copiaste el archivo `rentatools.war` a una USB o al Escritorio de otro computador:**
  Abres la terminal en esa misma carpeta donde pegaste el archivo y ejecutas:
  ```bash
  java -jar rentatools.war
  ```

---

### 3. Acceder a la Aplicación

Una vez que la terminal indique `Tomcat started on port 8080 (http) with context path '/'`:
* 🌐 **Plataforma Web (Frontend):** [http://localhost:8080/](http://localhost:8080/)
* 📚 **Documentación Swagger / OpenAPI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* 🚀 **API REST:** [http://localhost:8080/api/v1/tools](http://localhost:8080/api/v1/tools)

---

## 💾 Opciones de Base de Datos Local (Opcional)

Si en lugar de la base de datos en la nube de Supabase deseas trabajar con una base de datos local:

* **Opción A (Docker Compose - PostgreSQL 16):**
  ```bash
  docker compose up -d
  ```
* **Opción B (MySQL Local):**
  Crea la base de datos `renta_herramientas_db` en tu servidor MySQL 8 y ejecuta los scripts:
  ```bash
  mysql -u root -p < docs/sql/01_mysql_schema_4fn.sql
  mysql -u root -p < docs/sql/02_mysql_seeds_20_records.sql
  ```
  Y arranca pasando las variables de conexión local (`-DSPRING_DATASOURCE_URL=...`).

---

## 🚢 Despliegue en Servidor Apache Tomcat Externo (Solo si es exigido por el evaluador)

Si el profesor o evaluador te exige desplegar la aplicación dentro de una instalación independiente de Apache Tomcat:

1. Asegúrate de tener instalado **Apache Tomcat 10.1 o superior** (compatible con Jakarta EE 10 / Servlet 6.0).
2. Genera el empaquetado:
   ```bash
   ./mvnw clean package -DskipTests
   ```
3. Copia el archivo `infrastructure/target/rentatools.war` dentro de la carpeta `webapps/` de Apache Tomcat:
   ```bash
   # En Linux / macOS:
   cp infrastructure/target/rentatools.war /opt/tomcat/webapps/rentatools.war

   # En Windows:
   copy infrastructure\target\rentatools.war C:\apache-tomcat-10.1.x\webapps\rentatools.war
   ```
4. Inicia Tomcat (`bin/startup.sh` en Linux o `bin/startup.bat` en Windows).
5. Accede en tu navegador a: [http://localhost:8080/rentatools/](http://localhost:8080/rentatools/)

---

## 🧪 Pruebas Unitarias y de Integración

Para ejecutar la suite completa de pruebas automatizadas:

```bash
# Linux / macOS:
./mvnw test

# Windows:
mvnw.cmd test
```

> **Resultado:** 16 pruebas unitarias y de concurrencia ejecutadas con éxito (`0 Failures, 0 Errors, BUILD SUCCESS`) validando:
> * Autenticación y generación de JWT.
> * Algoritmo de detección de solapamiento de fechas.
> * Prevención de condiciones de carrera en reservas simultáneas.
> * Cálculo de impuestos y generación de PDFs con OpenPDF.
> * Búsqueda geoespacial Haversine.

---

## 📬 Pruebas de API con Postman

Se incluye la colección oficial con todas las peticiones organizadas por carpetas y variables dinámicas de autenticación:

* **Archivo de Colección:** [`docs/postman/RentaTools.postman_collection.json`](docs/postman/RentaTools.postman_collection.json)

### Pasos para usarla:
1. Abre Postman y haz clic en **Import**.
2. Selecciona el archivo `docs/postman/RentaTools.postman_collection.json`.
3. Ejecuta la petición **"Login Administrador"** o **"Login Cliente"** dentro de la carpeta *1. Autenticación*. El token JWT se guardará automáticamente en las variables del entorno para autorizar todas las demás consultas.

---

## 📑 Catálogo de Endpoints de la API REST

| Módulo | Método | Endpoint | Descripción | Acceso |
|---|---|---|---|---|
| **Auth** | `POST` | `/api/v1/auth/login` | Autenticación y obtención de JWT | Público |
| **Auth** | `POST` | `/api/v1/auth/register` | Registro de cliente o proveedor | Público |
| **Auth** | `GET` | `/api/v1/auth/me` | Información del perfil autenticado | Autenticado |
| **Herramientas** | `GET` | `/api/v1/tools` | Catálogo de maquinaria (filtros por categoría y estado) | Público |
| **Herramientas** | `GET` | `/api/v1/tools/{id}` | Detalle de herramienta y especificaciones | Público |
| **Herramientas** | `GET` | `/api/v1/tools/nearby` | Búsqueda geoespacial por radio (`lat`, `lng`, `radiusKm`) | Público |
| **Herramientas** | `POST` | `/api/v1/tools` | Registrar nueva herramienta en inventario | Proveedor / Admin |
| **Reservas** | `GET` | `/api/v1/rentals` | Listado general de alquileres | Autenticado |
| **Reservas** | `GET` | `/api/v1/rentals/my-rentals` | Alquileres del usuario en sesión | Cliente / Proveedor |
| **Reservas** | `GET` | `/api/v1/rentals/check-availability` | Comprobar disponibilidad en rango de fechas | Público |
| **Reservas** | `POST` | `/api/v1/rentals` | Crear solicitud de alquiler | Cliente |
| **Reservas** | `PUT` | `/api/v1/rentals/{id}/status` | Confirmar o rechazar alquiler | Proveedor / Admin |
| **Reservas** | `POST` | `/api/v1/rentals/{id}/return` | Registrar devolución, inspección y daños | Proveedor / Admin |
| **Reservas** | `GET` | `/api/v1/rentals/{id}/timeline` | Timeline de auditoría de estados de la reserva | Autenticado |
| **Carrito** | `GET` | `/api/v1/cart` | Obtener ítems del carrito en la nube | Cliente |
| **Carrito** | `POST` | `/api/v1/cart/items` | Agregar maquinaria al carrito | Cliente |
| **Carrito** | `DELETE`| `/api/v1/cart/items/{toolId}` | Eliminar ítem del carrito | Cliente |
| **Facturas** | `POST` | `/api/v1/invoices` | Generar factura electrónica con IVA 19% | Autenticado |
| **Facturas** | `GET` | `/api/v1/invoices/{id}` | Consultar factura por ID | Autenticado |
| **Facturas** | `GET` | `/api/v1/invoices/{id}/pdf` | Descargar factura oficial en PDF | Autenticado |
| **Pagos** | `POST` | `/api/v1/payments` | Procesar pago en línea (PSE / Tarjeta) | Cliente |
| **Reportes** | `GET` | `/api/v1/reports/dashboard` | Métricas globales de ingresos y ocupación | Admin / Proveedor |
| **Reportes** | `GET` | `/api/v1/reports/provider/{id}` | Estadísticas de facturación por proveedor | Proveedor / Admin |

---

### Ejemplo de Petición JSON: Crear Alquiler (`POST /api/v1/rentals`)
```json
{
  "toolId": 1,
  "startDate": "2026-11-10",
  "endDate": "2026-11-15",
  "deliveryAddress": "Proyecto Torre Titanium - Calle 100 # 15-20, Bogotá",
  "notes": "Requerida con operador certificado"
}
```

### Ejemplo de Respuesta JSON (`201 Created`):
```json
{
  "id": 26,
  "clientId": 1,
  "clientName": "Constructora Hábitat SAS",
  "toolId": 1,
  "toolName": "Retroexcavadora Oruga CAT 320D",
  "startDate": "2026-11-10",
  "endDate": "2026-11-15",
  "totalDays": 5,
  "dailyRate": 850000.00,
  "depositAmount": 2500000.00,
  "subtotal": 4250000.00,
  "status": "PENDIENTE",
  "deliveryAddress": "Proyecto Torre Titanium - Calle 100 # 15-20, Bogotá"
}
```

---

## 🌐 Swagger y Documentación Interactiva OpenAPI
Una vez iniciada la aplicación, puedes interactuar directamente con todos los endpoints en:
* **Interfaz Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Contrato OpenAPI v3:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 💻 Documentación Adicional

* [**Arquitectura DDD y Patrones de Concurrencia**](docs/ARCHITECTURE_DDD.md)
* [**Diseño y Normalización de Base de Datos en 4FN**](docs/DATABASE_SETUP_4FN.md)
* [**Seguridad JWT, Roles y Matriz de Acceso**](docs/JWT_SECURITY_ROLES.md)
* [**Contratos de la API REST y Swagger**](docs/SWAGGER_API_CONTRACTS.md)
* [**Colección Oficial de Postman**](docs/postman/RentaTools.postman_collection.json)
