# 🔐 Seguridad, Autenticación JWT y Control de Acceso (RBAC)

La seguridad del sistema está construida sobre **Spring Security 6** en modo **Stateless** (sin sesiones de servidor HTTP), utilizando tokens criptográficos **JSON Web Tokens (JWT)** firmados mediante el algoritmo **HMAC-SHA256**.

---

## 👥 Roles del Sistema y Matriz de Permisos

| Rol | Identificador | Alcance y Funcionalidades |
|---|---|---|
| **Administrador** | `ROLE_ADMIN` | Supervisión global del marketplace, métricas de ingresos, trazabilidad de toda la flota en el mapa nacional, auditoría de devoluciones e incidencias. |
| **Proveedor** | `ROLE_PROVEEDOR` | Registro y edición de maquinaria en inventario, aprobación o rechazo de solicitudes de alquiler, confirmación de entregas y reporte de daños. |
| **Cliente** | `ROLE_CLIENTE` | Exploración de maquinaria por cercanía geográfica, cotizaciones, carrito de compras multi-equipo, confirmación de reserva, pasarela de pago y descarga de facturas en PDF. |

---

## 🔑 Credenciales Predeterminadas de Prueba

| Rol | Usuario | Contraseña | Nombre de la Entidad |
|---|---|---|---|
| **Admin** | `admin` | `Admin123!` | Carlos Mendoza (Director General RentaTools) |
| **Proveedor** | `proveedor` | `Proveedor123!` | Ing. Alejandro Morales (Maquinaria Andina SAS) |
| **Proveedor 2** | `proveedor2` | `Proveedor123!` | Dr. Gabriel Jaramillo (Gecolsa Maquinaria Pesada SAS) |
| **Cliente** | `cliente` | `Cliente123!` | Ing. Cristian Muñoz (Constructora Hábitat SAS) |
| **Cliente 2** | `cliente2` | `Cliente123!` | Ing. Roberto Camargo (Amarilo Construcciones SAS) |

---

## 🛡️ Estructura del Token JWT

* **Header:** Algoritmo `HS256`, Tipo `JWT`.
* **Payload:**
  * `sub`: Nombre de usuario (`username`).
  * `role`: Rol del usuario (`ROLE_ADMIN`, `ROLE_PROVEEDOR`, `ROLE_CLIENTE`).
  * `userId`: Identificador numérico único.
  * `iat`: Fecha de emisión.
  * `exp`: Fecha de expiración (24 horas por defecto).
* **Firma:** Validada en cada petición entrante mediante [`JwtAuthFilter`](file:///home/Papi_Leo/VSCODE/SpringBoot/Proyecto_RentaHerramientas/infrastructure/src/main/java/com/rentatools/infrastructure/security/JwtAuthFilter.java).
