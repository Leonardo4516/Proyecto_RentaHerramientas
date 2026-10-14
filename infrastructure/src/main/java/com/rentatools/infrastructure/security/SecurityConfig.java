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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Frontend estático y recursos web
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico").permitAll()
                        // Documentación Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // WebSockets STOMP
                        .requestMatchers("/ws-rentatools/**").permitAll()
                        // Autenticación pública
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Catálogo público de herramientas (Dev A y fallback)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tools/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tools/**").hasAnyAuthority("ROLE_PROVEEDOR", "ROLE_ADMIN")
                        // Descarga pública o autorizada de facturas PDF
                        .requestMatchers(HttpMethod.GET, "/api/v1/invoices/*/pdf").permitAll()
                        // Endpoints de reportes protegidos
                        .requestMatchers("/api/v1/reports/dashboard").hasAnyAuthority("ROLE_ADMIN", "ROLE_PROVEEDOR")
                        .requestMatchers("/api/v1/reports/provider/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_PROVEEDOR")
                        // Pagos y facturación protegidos
                        .requestMatchers("/api/v1/payments/**").authenticated()
                        .requestMatchers("/api/v1/invoices/**").authenticated()
                        // Alquileres protegidos
                        .requestMatchers("/api/v1/rentals/**").authenticated()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
