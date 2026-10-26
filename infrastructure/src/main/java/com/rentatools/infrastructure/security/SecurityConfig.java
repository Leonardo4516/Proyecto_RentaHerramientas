package com.rentatools.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
                            String json = String.format(
                                    "{\"status\":401,\"error\":\"No autorizado\",\"message\":\"Sesión expirada o no autenticada. Por favor, inicia sesión nuevamente.\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                                    request.getRequestURI(),
                                    java.time.Instant.now()
                            );
                            response.getWriter().write(json);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
                            String json = String.format(
                                    "{\"status\":403,\"error\":\"Acceso denegado\",\"message\":\"No tienes permisos suficientes para realizar esta acción.\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                                    request.getRequestURI(),
                                    java.time.Instant.now()
                            );
                            response.getWriter().write(json);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Consola H2 en memoria para entornos de prueba
                        .requestMatchers("/h2-console/**").permitAll()
                        // Frontend estático y recursos web
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/assets/**", "/models/**", "/favicon.ico").permitAll()
                        // Documentación Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // WebSockets STOMP
                        .requestMatchers("/ws-rentatools/**").permitAll()
                        // Autenticación pública
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Catálogo público de herramientas (Dev A y alias en español)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tools/**", "/api/herramientas/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tools/**").hasAnyAuthority("ROLE_PROVEEDOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/herramientas/**").hasAnyAuthority("ROLE_PROVEEDOR", "ROLE_ADMIN")
                        // Descarga pública o autorizada de facturas PDF
                        .requestMatchers(HttpMethod.GET, "/api/v1/invoices/*/pdf").permitAll()
                        // Endpoints de reportes protegidos
                        .requestMatchers("/api/v1/reports/dashboard").hasAnyAuthority("ROLE_ADMIN", "ROLE_PROVEEDOR")
                        .requestMatchers("/api/v1/reports/provider/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_PROVEEDOR")
                        // Pagos y facturación protegidos
                        .requestMatchers("/api/v1/payments/**").authenticated()
                        .requestMatchers("/api/v1/invoices/**").authenticated()
                        // Alquileres públicos de consulta de disponibilidad y agenda
                        .requestMatchers(HttpMethod.GET, "/api/v1/rentals/check-availability", "/api/v1/rentals/tool/**").permitAll()
                        // Alquileres protegidos (estándar y alias en español)
                        .requestMatchers("/api/v1/rentals/**", "/api/reservas/**").authenticated()
                        // Cupones y reseñas públicas
                        .requestMatchers(HttpMethod.GET, "/api/v1/coupons/**", "/api/v1/reviews/tool/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").authenticated()
                        // Carrito de compras protegido en la nube
                        .requestMatchers("/api/v1/cart/**").authenticated()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
