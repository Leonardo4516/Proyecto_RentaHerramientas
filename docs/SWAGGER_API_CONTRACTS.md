# 📋 Catálogo de Endpoints REST y Contratos de la API

La API REST de **RentaTools** se encuentra documentada e interactiva bajo el estándar **OpenAPI 3 / Swagger UI**.

* **Acceso Interactivo Swagger:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Especificación OpenAPI en JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🚀 Resumen de Endpoints Principales

### 1. Autenticación (`/api/v1/auth`)
* `POST /api/v1/auth/login`: Iniciar sesión (Retorna token JWT).
* `POST /api/v1/auth/register`: Registro de nuevo cliente o proveedor.
* `GET /api/v1/auth/me`: Consultar perfil del usuario autenticado vía token.

### 2. Maquinaria y Catálogo (`/api/v1/tools`)
* `GET /api/v1/tools`: Listado público de herramientas (Filtros por `categoryId` y `status`).
* `GET /api/v1/tools/{id}`: Detalle completo de una máquina con especificaciones y fotos.
* `GET /api/v1/tools/nearby`: Búsqueda por proximidad geoespacial (`lat`, `lng`, `radiusKm`).
* `GET /api/v1/tools/categories`: Listado de categorías activas.
* `POST /api/v1/tools`: Alta de nueva herramienta en inventario (`ROLE_PROVEEDOR`, `ROLE_ADMIN`).

### 3. Reservas y Alquileres (`/api/v1/rentals`)
* `GET /api/v1/rentals`: Listar reservas (con aislamiento tenant por rol).
* `GET /api/v1/rentals/my-rentals`: Reservas asociadas al usuario autenticado.
* `GET /api/v1/rentals/check-availability`: Comprobación atómica de no solapamiento.
* `POST /api/v1/rentals`: Crear solicitud de alquiler.
* `PUT /api/v1/rentals/{id}/status`: Aprobación/rechazo de reserva.
* `POST /api/v1/rentals/{id}/return`: Acta de devolución técnica, evaluación de daños y cálculo de depósito.
* `GET /api/v1/rentals/{id}/timeline`: Historial cronológico de cambios de estado (Auditoría).

### 4. Carrito en la Nube (`/api/v1/cart`)
* `GET /api/v1/cart`: Consulta del carrito activo del usuario autenticado.
* `POST /api/v1/cart/items`: Agregar maquinaria al carrito con rango de fechas.
* `DELETE /api/v1/cart/items/{toolId}`: Remover un ítem del carrito.
* `DELETE /api/v1/cart`: Vaciar el carrito completo.

### 5. Facturación y PDF (`/api/v1/invoices`)
* `POST /api/v1/invoices`: Emisión de factura con desglose fiscal (IVA 19%).
* `GET /api/v1/invoices/{id}`: Consulta de factura por ID.
* `GET /api/v1/invoices/client/{clientId}`: Facturas de un cliente.
* `GET /api/v1/invoices/{id}/pdf`: Descarga oficial del comprobante en PDF con OpenPDF.

### 6. Pagos (`/api/v1/payments`)
* `POST /api/v1/payments`: Registro y procesamiento de pago en línea (PSE / Tarjeta).
* `GET /api/v1/payments/invoice/{invoiceId}`: Comprobante de pago por factura.

### 7. Reportes y Métricas (`/api/v1/reports`)
* `GET /api/v1/reports/dashboard`: KPIs consolidados de ingresos, ocupación y actividad (Admin).
* `GET /api/v1/reports/provider/{providerId}`: Estadísticas de facturación y ocupación por proveedor.
