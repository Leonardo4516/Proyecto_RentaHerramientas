package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.tool.dto.NearbyToolResponse;
import com.rentatools.application.tool.dto.RegisterToolCommand;
import com.rentatools.application.tool.dto.ToolCategoryResponse;
import com.rentatools.application.tool.dto.ToolResponse;
import com.rentatools.application.tool.usecase.FindNearbyToolsUseCase;
import com.rentatools.application.tool.usecase.GetToolByIdUseCase;
import com.rentatools.application.tool.usecase.ListToolsUseCase;
import com.rentatools.application.tool.usecase.RegisterToolUseCase;
import com.rentatools.domain.tool.ToolCategoryRepository;
import com.rentatools.domain.tool.ToolStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tools")
@Tag(name = "Herramientas e Inventario", description = "Endpoints para consulta, catálogo, geolocalización y registro de maquinaria")
public class ToolController {

    private final ListToolsUseCase listToolsUseCase;
    private final GetToolByIdUseCase getToolByIdUseCase;
    private final RegisterToolUseCase registerToolUseCase;
    private final FindNearbyToolsUseCase findNearbyToolsUseCase;
    private final ToolCategoryRepository categoryRepository;

    public ToolController(ListToolsUseCase listToolsUseCase,
                          GetToolByIdUseCase getToolByIdUseCase,
                          RegisterToolUseCase registerToolUseCase,
                          FindNearbyToolsUseCase findNearbyToolsUseCase,
                          ToolCategoryRepository categoryRepository) {
        this.listToolsUseCase = listToolsUseCase;
        this.getToolByIdUseCase = getToolByIdUseCase;
        this.registerToolUseCase = registerToolUseCase;
        this.findNearbyToolsUseCase = findNearbyToolsUseCase;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/public")
    @Operation(summary = "Catálogo público de herramientas", description = "Permite filtrar por categoría y estado de disponibilidad")
    public ResponseEntity<List<ToolResponse>> listTools(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ToolStatus status
    ) {
        List<ToolResponse> tools = listToolsUseCase.execute(categoryId, status);
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una herramienta por ID")
    public ResponseEntity<ToolResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getToolByIdUseCase.execute(id));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Buscar maquinaria cercana (Geolocalización)", description = "Calcula la distancia Haversine a la obra del cliente y filtra por radio en km")
    public ResponseEntity<List<NearbyToolResponse>> findNearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "30.0") double radiusKm
    ) {
        List<NearbyToolResponse> nearby = findNearbyToolsUseCase.execute(lat, lng, radiusKm);
        return ResponseEntity.ok(nearby);
    }

    @GetMapping("/categories")
    @Operation(summary = "Listar categorías activas de herramientas")
    public ResponseEntity<List<ToolCategoryResponse>> listCategories() {
        List<ToolCategoryResponse> categories = categoryRepository.findAllActive().stream()
                .map(ToolCategoryResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @Operation(summary = "Registrar nueva herramienta en el inventario (Rol: Proveedor)")
    public ResponseEntity<ToolResponse> registerTool(@Valid @RequestBody RegisterToolCommand command) {
        ToolResponse created = registerToolUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
