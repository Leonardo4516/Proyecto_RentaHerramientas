package com.rentatools.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Plataforma de Renta de Herramientas y Equipos - API REST",
                version = "1.0.0",
                description = "Catálogo de endpoints empresariales para gestión de reservas, autenticación JWT, facturación electrónica y notificaciones en tiempo real.",
                contact = @Contact(name = "Equipo de Desarrollo RentaTools", email = "soporte@rentatools.com"),
                license = @License(name = "Apache 2.0", url = "https://spring.io")
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresa el token JWT obtenido en /api/v1/auth/login"
)
public class OpenApiConfig {
}
