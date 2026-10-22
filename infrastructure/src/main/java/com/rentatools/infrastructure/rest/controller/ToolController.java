package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.tool.dto.NearbyToolResponse;
import com.rentatools.application.tool.dto.RegisterToolCommand;
import com.rentatools.application.tool.dto.ToolCategoryResponse;
import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.application.tool.dto.UpdateToolCommand;
import com.rentatools.application.tool.usecase.FindNearbyToolsUseCase;
import com.rentatools.application.tool.usecase.GetToolByIdUseCase;
import com.rentatools.application.tool.usecase.ListToolsUseCase;
import com.rentatools.application.tool.usecase.RegisterToolUseCase;
import com.rentatools.application.tool.usecase.SearchToolsUseCase;
import com.rentatools.application.tool.usecase.UpdateToolUseCase;
import com.rentatools.domain.tool.ToolCategoryRepository;
import com.rentatools.domain.tool.ToolStatus;
import com.rentatools.infrastructure.persistence.user.ProviderJpaEntity;
import com.rentatools.infrastructure.persistence.user.SpringDataProviderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentatools.domain.common.exception.UnauthorizedException;
import com.rentatools.infrastructure.persistence.user.SpringDataUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
@RestController
@RequestMapping("/api/v1/tools")
@Tag(name = "Herramientas e Inventario", description = "Endpoints para consulta, catálogo, geolocalización y registro de maquinaria")
public class ToolController {

    private final ListToolsUseCase listToolsUseCase;
    private final SearchToolsUseCase searchToolsUseCase;
    private final GetToolByIdUseCase getToolByIdUseCase;
    private final RegisterToolUseCase registerToolUseCase;
    private final UpdateToolUseCase updateToolUseCase;
    private final FindNearbyToolsUseCase findNearbyToolsUseCase;
    private final com.rentatools.application.tool.usecase.CreateToolCategoryUseCase createToolCategoryUseCase;
    private final com.rentatools.application.tool.usecase.UpdateToolCategoryUseCase updateToolCategoryUseCase;
    private final com.rentatools.application.tool.usecase.RegisterToolMaintenanceUseCase registerToolMaintenanceUseCase;
    private final com.rentatools.application.tool.usecase.GetToolMaintenanceHistoryUseCase getToolMaintenanceHistoryUseCase;
    private final ToolCategoryRepository categoryRepository;
    private final SpringDataProviderRepository providerRepository;
    private final SpringDataUserRepository userRepository;

    public ToolController(ListToolsUseCase listToolsUseCase,
                          SearchToolsUseCase searchToolsUseCase,
                          GetToolByIdUseCase getToolByIdUseCase,
                          RegisterToolUseCase registerToolUseCase,
                          UpdateToolUseCase updateToolUseCase,
                          FindNearbyToolsUseCase findNearbyToolsUseCase,
                          com.rentatools.application.tool.usecase.CreateToolCategoryUseCase createToolCategoryUseCase,
                          com.rentatools.application.tool.usecase.UpdateToolCategoryUseCase updateToolCategoryUseCase,
                          com.rentatools.application.tool.usecase.RegisterToolMaintenanceUseCase registerToolMaintenanceUseCase,
                          com.rentatools.application.tool.usecase.GetToolMaintenanceHistoryUseCase getToolMaintenanceHistoryUseCase,
                          ToolCategoryRepository categoryRepository,
                          SpringDataProviderRepository providerRepository,
                          SpringDataUserRepository userRepository) {
        this.listToolsUseCase = listToolsUseCase;
        this.searchToolsUseCase = searchToolsUseCase;
        this.getToolByIdUseCase = getToolByIdUseCase;
        this.registerToolUseCase = registerToolUseCase;
        this.updateToolUseCase = updateToolUseCase;
        this.findNearbyToolsUseCase = findNearbyToolsUseCase;
        this.createToolCategoryUseCase = createToolCategoryUseCase;
        this.updateToolCategoryUseCase = updateToolCategoryUseCase;
        this.registerToolMaintenanceUseCase = registerToolMaintenanceUseCase;
        this.getToolMaintenanceHistoryUseCase = getToolMaintenanceHistoryUseCase;
        this.categoryRepository = categoryRepository;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    @Operation(summary = "Búsqueda multifactorial de maquinaria", description = "Busca por texto libre en nombre, marca, modelo o código, con filtros opcionales de categoría, rango de precios y estado")
    public ResponseEntity<List<ToolResponse>> searchTools(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ToolStatus status
    ) {
        List<ToolResponse> tools = searchToolsUseCase.execute(q, categoryId, minPrice, maxPrice, status).stream()
                .map(this::enrichTool)
                .toList();
        return ResponseEntity.ok(tools);
    }

    @GetMapping({"", "/public"})
    @Operation(summary = "Catálogo público de herramientas", description = "Permite filtrar por categoría y estado de disponibilidad")
    public ResponseEntity<List<ToolResponse>> listTools(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ToolStatus status
    ) {
        List<ToolResponse> tools = listToolsUseCase.execute(categoryId, status).stream()
                .map(this::enrichTool)
                .toList();
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una herramienta por ID")
    public ResponseEntity<ToolResponse> getById(@PathVariable Long id) {
        ToolResponse res = getToolByIdUseCase.execute(id);
        return ResponseEntity.ok(enrichTool(res));
    }

    private ToolResponse enrichTool(ToolResponse t) {
        if (t == null) return null;
        if (providerRepository == null || t.providerId() == null) return t;
        String pName = providerRepository.findById(t.providerId())
                .map(ProviderJpaEntity::getCompanyName)
                .orElse("Proveedor Verificado");
        return new ToolResponse(
                t.id(),
                t.providerId(),
                t.categoryId(),
                t.code(),
                t.name(),
                t.brand(),
                t.model(),
                t.dailyPrice(),
                t.depositAmount(),
                t.status(),
                t.accumulatedRentalDays(),
                t.latitude(),
                t.longitude(),
                t.createdAt(),
                t.images(),
                t.specifications(),
                pName
        );
    }

    @GetMapping("/nearby")
    @Operation(summary = "Buscar maquinaria cercana (Geolocalización)", description = "Calcula la distancia Haversine a la obra del cliente y filtra por radio en km")
    public ResponseEntity<List<NearbyToolResponse>> findNearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "30.0") double radiusKm
    ) {
        List<NearbyToolResponse> nearby = findNearbyToolsUseCase.execute(lat, lng, radiusKm);
        List<NearbyToolResponse> enriched = nearby.stream().map(dto -> {
            if (dto.providerId() != null && providerRepository != null) {
                var pOpt = providerRepository.findById(dto.providerId());
                if (pOpt.isPresent() && pOpt.get().getCompanyName() != null) {
                    return new NearbyToolResponse(
                            dto.id(), dto.code(), dto.name(), dto.brand(), dto.model(),
                            dto.dailyPrice(), dto.depositAmount(), dto.status(),
                            dto.latitude(), dto.longitude(), dto.distanceKm(), dto.coverImageUrl(),
                            dto.providerId(), pOpt.get().getCompanyName()
                    );
                }
            }
            return dto;
        }).toList();
        return ResponseEntity.ok(enriched);
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar categorías activas de herramientas")
    public ResponseEntity<List<ToolCategoryResponse>> listCategories() {
        List<ToolCategoryResponse> categories = categoryRepository.findAllActive().stream()
                .map(ToolCategoryResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Crear nueva categoría de herramientas (Rol: Admin)")
    public ResponseEntity<ToolCategoryResponse> createCategory(
            @Valid @RequestBody com.rentatools.application.tool.dto.CreateToolCategoryCommand command
    ) {
        ToolCategoryResponse created = createToolCategoryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Actualizar categoría de herramientas (Rol: Admin)")
    public ResponseEntity<ToolCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody com.rentatools.application.tool.dto.UpdateToolCategoryCommand command
    ) {
        ToolCategoryResponse updated = updateToolCategoryUseCase.execute(id, command);
        return ResponseEntity.ok(updated);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Registrar nueva herramienta en el inventario (Rol: Proveedor)")
    public ResponseEntity<ToolResponse> registerTool(
            @Valid @RequestBody RegisterToolCommand command,
            Authentication authentication
    ) {
        RegisterToolCommand effectiveCommand = command;
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            String username = authentication.getName();
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                var provOpt = providerRepository.findByUserId(userOpt.get().getId());
                if (provOpt.isPresent()) {
                    Long authProviderId = provOpt.get().getId();
                    effectiveCommand = new RegisterToolCommand(
                            authProviderId,
                            command.categoryId(),
                            command.code(),
                            command.name(),
                            command.brand(),
                            command.model(),
                            command.dailyPrice(),
                            command.depositAmount(),
                            command.latitude(),
                            command.longitude(),
                            command.imageUrls(),
                            command.specifications()
                    );
                }
            }
        }
        ToolResponse created = registerToolUseCase.execute(effectiveCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrichTool(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Actualizar datos y tarifas de una herramienta", description = "Permite modificar nombre, marca, modelo, tarifa diaria y depósito de garantía de una herramienta")
    public ResponseEntity<ToolResponse> updateTool(
            @PathVariable Long id,
            @Valid @RequestBody UpdateToolCommand command,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateToolProviderOwnership(id, authentication.getName());
        }
        ToolResponse updated = updateToolUseCase.execute(id, command);
        return ResponseEntity.ok(enrichTool(updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Actualizar estado operativo de una herramienta", description = "Conmuta el estado operativo entre DISPONIBLE, EN_MANTENIMIENTO y DE_BAJA")
    public ResponseEntity<ToolResponse> updateToolStatus(
            @PathVariable Long id,
            @RequestParam ToolStatus status,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateToolProviderOwnership(id, authentication.getName());
        }
        ToolResponse updated = updateToolUseCase.updateStatus(id, status);
        return ResponseEntity.ok(enrichTool(updated));
    }

    @PostMapping("/{id}/maintenance")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Registrar mantenimiento preventivo o correctivo de maquinaria", description = "Registra una entrada en la bitácora técnica de la herramienta y opcionalmente conmuta su estado a EN_MANTENIMIENTO")
    public ResponseEntity<com.rentatools.application.tool.dto.ToolMaintenanceResponse> registerMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody com.rentatools.application.tool.dto.RegisterMaintenanceCommand command,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateToolProviderOwnership(id, authentication.getName());
        }
        com.rentatools.application.tool.dto.ToolMaintenanceResponse response = registerToolMaintenanceUseCase.execute(id, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/maintenance")
    @Operation(summary = "Consultar bitácora de mantenimientos de una herramienta")
    public ResponseEntity<List<com.rentatools.application.tool.dto.ToolMaintenanceResponse>> getMaintenanceHistory(
            @PathVariable Long id
    ) {
        List<com.rentatools.application.tool.dto.ToolMaintenanceResponse> history = getToolMaintenanceHistoryUseCase.execute(id);
        return ResponseEntity.ok(history);
    }

    private void validateToolProviderOwnership(Long toolId, String username) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Usuario autenticado no válido.");
        }
        var provOpt = providerRepository.findByUserId(userOpt.get().getId());
        if (provOpt.isEmpty()) {
            throw new UnauthorizedException("El usuario no tiene un perfil de proveedor asociado.");
        }
        ToolResponse tool = getToolByIdUseCase.execute(toolId);
        if (tool != null && !provOpt.get().getId().equals(tool.providerId())) {
            throw new UnauthorizedException("No tienes autorización para modificar maquinaria perteneciente a otra empresa.");
        }
    }
}
