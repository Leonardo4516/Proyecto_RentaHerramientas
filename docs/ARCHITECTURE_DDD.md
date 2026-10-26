# 🏛️ Arquitectura Hexagonal y Domain-Driven Design (DDD)

La plataforma **RentaTools** está diseñada siguiendo los principios de **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Domain-Driven Design (DDD)** para desacoplar las reglas de negocio críticas del framework de persistencia, la infraestructura HTTP y librerías externas.

---

## 📂 Estructura Multi-Módulo

El proyecto está organizado en 3 módulos Maven independientes:

```text
Proyecto_RentaHerramientas/
├── domain/                  # Núcleo Puro de Dominio (Sin dependencias de Spring Boot ni frameworks)
│   ├── src/main/java/com/rentatools/domain/
│   │   ├── auth/            # Entidades y Puertos de Autenticación
│   │   ├── billing/         # Modelos de Factura, Ítems de Factura y Puertos
│   │   ├── rental/          # Agregados de Alquiler, Rango de Fechas (Value Object) y Puertos
│   │   ├── tool/            # Entidad Herramienta, Categoría, Coordenadas (Value Object) y Puertos
│   │   └── user/            # Entidad Usuario, Perfiles (Admin, Proveedor, Cliente) y Roles
│   └── src/test/            # Pruebas Unitarias de Reglas de Negocio Puras
│
├── application/             # Capa de Aplicación (Casos de Uso y DTOs)
│   ├── src/main/java/com/rentatools/application/
│   │   ├── auth/            # LoginUseCase, RegisterUseCase, GetCurrentUserUseCase
│   │   ├── billing/         # GenerateInvoiceUseCase, ProcessPaymentUseCase, DownloadPdfUseCase
│   │   ├── rental/          # CreateRentalBookingUseCase, CheckAvailabilityUseCase, ChangeRentalStatusUseCase
│   │   ├── report/          # GetDashboardMetricsUseCase, GetProviderStatsUseCase
│   │   └── tool/            # ListToolsUseCase, GetToolByIdUseCase, RegisterToolUseCase, FindNearbyToolsUseCase
│   └── src/test/            # Pruebas de Casos de Uso con Mocks (Mockito)
│
└── infrastructure/          # Adaptadores de Entrada/Salida y Configuración de Framework
    ├── src/main/java/com/rentatools/infrastructure/
    │   ├── adapters/        # Controladores REST de Entrada y Adaptadores de Persistencia
    │   ├── pdf/             # Adaptador de Generación de Facturas PDF con OpenPDF
    │   ├── persistence/     # Entidades JPA, Repositorios Spring Data e Índices
    │   ├── security/        # Filtro JWT, UserDetailsService y Configuración Spring Security 6
    │   └── websocket/       # Configuración STOMP y Servicio de Notificaciones en Tiempo Real
    └── src/main/resources/  # application.yml y Assets Estáticos Frontend (HTML/CSS/JS)
```

---

## 🔒 Reglas Clave de Concurrencia y Dominio

1. **Prevención Atómica de Solapamiento de Alquileres:**
   * Utiliza el Value Object [`DateRange`](file:///home/Papi_Leo/VSCODE/SpringBoot/Proyecto_RentaHerramientas/domain/src/main/java/com/rentatools/domain/rental/DateRange.java) con la fórmula matemática de no intersección:
     $$\text{Solapamiento} \iff (\text{NuevaInicio} \le \text{ExistenteFin}) \land (\text{NuevaFin} \ge \text{ExistenteInicio})$$
   * La transacción en base de datos corre con nivel de aislamiento **`REPEATABLE_READ`** y bloqueo pesimista de fila para erradicar condiciones de carrera (*Race Conditions*).

2. **Cálculo de Distancias Geoespaciales:**
   * Implementación de la **Fórmula de Haversine** dentro de [`GeoCoordinates`](file:///home/Papi_Leo/VSCODE/SpringBoot/Proyecto_RentaHerramientas/domain/src/main/java/com/rentatools/domain/tool/GeoCoordinates.java) para calcular el radio de cobertura en kilómetros entre la maquinaria del proveedor y la obra del cliente.
