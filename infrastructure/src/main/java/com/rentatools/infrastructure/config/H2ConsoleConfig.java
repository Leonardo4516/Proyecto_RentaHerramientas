package com.rentatools.infrastructure.config;

import org.h2.server.web.JakartaWebServlet;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * Configuración explícita de la consola web H2 y del servidor TCP para H2.
 * Permite que evaluadores y desarrolladores accedan tanto por navegador web
 * (/h2-console) como mediante herramientas externas como DBeaver (puerto 9092).
 */
@Configuration
@ConditionalOnClass(JakartaWebServlet.class)
@ConditionalOnProperty(prefix = "spring.h2.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class H2ConsoleConfig {

    private static final Logger log = LoggerFactory.getLogger(H2ConsoleConfig.class);

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServletRegistration() {
        ServletRegistrationBean<JakartaWebServlet> registration = new ServletRegistrationBean<>(
                new JakartaWebServlet(),
                "/h2-console",
                "/h2-console/*"
        );
        registration.setName("H2ConsoleServlet");
        registration.addInitParameter("webAllowOthers", "true");
        registration.setLoadOnStartup(1);
        log.info("🔌 [H2 Console] Registrado exitosamente en /h2-console (webAllowOthers=true)");
        return registration;
    }

    /**
     * Servidor TCP en puerto 9092 que permite conectar herramientas externas
     * como DBeaver, IntelliJ Database o DataGrip a la base de datos H2 en memoria:
     * JDBC URL: jdbc:h2:tcp://localhost:9092/mem:rentatools
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() {
        try {
            Server server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists");
            log.info("🌐 [H2 TCP Server] Iniciado en puerto 9092. DBeaver puede conectarse vía: jdbc:h2:tcp://localhost:9092/mem:rentatools");
            return server;
        } catch (SQLException e) {
            log.warn("⚠️ [H2 TCP Server] Puerto 9092 ocupado o no disponible para servidor TCP H2: {}", e.getMessage());
            return null;
        }
    }
}
