package com.rentatools.infrastructure;

import com.rentatools.application.auth.port.PasswordEncoderPort;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.user.ClientProfile;
import com.rentatools.domain.user.ProviderProfile;
import com.rentatools.domain.user.Role;
import com.rentatools.domain.user.User;
import com.rentatools.domain.user.port.UserRepository;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication(scanBasePackages = {"com.rentatools"})
public class RentaToolsApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(RentaToolsApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RentaToolsApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(RentaToolsApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedInitialData(UserRepository userRepository,
                                             PasswordEncoderPort passwordEncoder,
                                             InvoiceRepository invoiceRepository,
                                             ToolJpaRepository toolRepository,
                                             DataSource dataSource) {
        return args -> {
            log.info("Inicializando datos del sistema RentaTools...");

            // Cargar catálogo completo de herramientas (25 herramientas, categorías, specs, fotos, proveedores) si está vacío
            try {
                if (toolRepository.count() == 0) {
                    log.info("Catálogo vacío detectado. Cargando 25 herramientas, categorías y datos desde catalog_seeds.sql...");
                    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                    populator.setContinueOnError(true);
                    populator.setIgnoreFailedDrops(true);
                    populator.addScript(new ClassPathResource("seeds/catalog_seeds.sql"));
                    populator.execute(dataSource);
                    log.info("Catálogo de herramientas cargado exitosamente. Total disponibles: {}", toolRepository.count());
                }
            } catch (Exception e) {
                log.warn("Aviso cargando script de herramientas: {}", e.getMessage());
            }

            // Sincronizar secuencias e identidades para evitar colisión de claves primarias en nuevas reservas
            try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
                String[] tables = {"rentals", "users", "tools", "tool_categories", "tool_images",
                                   "tool_specifications", "invoices", "invoice_items", "payments",
                                   "cart_items", "clients", "providers", "admins", "rental_audit_logs"};
                for (String table : tables) {
                    try {
                        stmt.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH 1000");
                    } catch (Exception ignored) {
                        try {
                            stmt.execute("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), COALESCE(max(id), 1) + 1) FROM " + table);
                        } catch (Exception ignored2) {}
                    }
                }
                log.info("Secuencias e identidades de tablas sincronizadas correctamente.");
            } catch (Exception e) {
                log.warn("Aviso sincronizando secuencias de base de datos: {}", e.getMessage());
            }

            // 1. Usuario Administrador
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@rentatools.com");
                admin.setPassword(passwordEncoder.encode("Admin123!"));
                admin.setFullName("Administrador General del Sistema");
                admin.setPhone("+57 300 1234567");
                admin.setRole(Role.ROLE_ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
                log.info("Usuario ADMIN creado: admin / Admin123!");
            }

            // 2. Usuario Proveedor
            if (!userRepository.existsByUsername("proveedor")) {
                User provider = new User();
                provider.setUsername("proveedor");
                provider.setEmail("proveedor@rentatools.com");
                provider.setPassword(passwordEncoder.encode("Proveedor123!"));
                provider.setFullName("ConstruMaquinaria del Norte SAS");
                provider.setPhone("+57 310 9876543");
                provider.setRole(Role.ROLE_PROVEEDOR);
                provider.setActive(true);

                ProviderProfile pp = new ProviderProfile();
                pp.setBusinessName("ConstruMaquinaria del Norte SAS");
                pp.setTaxId("901.888.777-4");
                pp.setCommercialRegistry("REG-CAM-54210");
                pp.setAddress("Calle 170 # 15-20");
                pp.setCity("Bogotá");
                pp.setRating(4.9);
                pp.setVerified(true);
                provider.setProviderProfile(pp);

                userRepository.save(provider);
                log.info("Usuario PROVEEDOR creado: proveedor / Proveedor123!");
            }

            // 3. Usuario Cliente
            User savedClient = null;
            if (!userRepository.existsByUsername("cliente")) {
                User client = new User();
                client.setUsername("cliente");
                client.setEmail("cliente@constructora.com");
                client.setPassword(passwordEncoder.encode("Cliente123!"));
                client.setFullName("Cristian Muñoz Constructora SAS");
                client.setPhone("+57 315 5558899");
                client.setRole(Role.ROLE_CLIENTE);
                client.setActive(true);

                ClientProfile cp = new ClientProfile();
                cp.setCompanyName("Cristian Muñoz Constructora SAS");
                cp.setTaxId("900.765.432-1");
                cp.setAddress("Carrera 7 # 116-50");
                cp.setCity("Bogotá");
                cp.setPreferredPaymentMethod("PSE");
                client.setClientProfile(cp);

                savedClient = userRepository.save(client);
                log.info("Usuario CLIENTE creado: cliente / Cliente123!");
            } else {
                savedClient = userRepository.findByUsername("cliente").orElse(null);
            }

            // 4. Factura inicial semilla para el cliente
            if (savedClient != null && invoiceRepository.findByRentalId(1L).isEmpty()) {
                Invoice invoice = new Invoice();
                invoice.setInvoiceNumber("INV-202610-1001");
                invoice.setRentalId(1L);
                invoice.setClientId(savedClient.getId());
                invoice.setClientName(savedClient.getFullName());
                invoice.setClientTaxId("900.765.432-1");
                invoice.setDepositAmount(new BigDecimal("1200000"));
                invoice.setIssueDate(LocalDateTime.now().minusDays(1));
                invoice.setDueDate(LocalDateTime.now().plusDays(2));

                InvoiceItem item = new InvoiceItem(null, 1L, "Retroexcavadora CAT 420F2", new BigDecimal("450000"), 3);
                invoice.addItem(item);
                invoice.recalculateTotals();

                invoiceRepository.save(invoice);
                log.info("Factura semilla INV-202610-1001 creada exitosamente con total: ${}", invoice.getTotalAmount());
            }

            log.info("Semillas cargadas correctamente.");
        };
    }
}
