package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.tool.dto.ToolResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/herramientas")
@Tag(name = "Herramientas (Alias Examen en Español)", description = "Controlador alias para compatibilidad exacta con enunciados de evaluación técnica")
public class HerramientaController {

    private final ToolController toolController;

    public HerramientaController(ToolController toolController) {
        this.toolController = toolController;
    }

    @PutMapping("/{id}/mantenimiento")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Enviar maquinaria a mantenimiento (Rol: Proveedor)")
    public ResponseEntity<ToolResponse> enviarAMantenimiento(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Mantenimiento preventivo") String motivo,
            Authentication authentication
    ) {
        return toolController.enviarAMantenimiento(id, motivo, authentication);
    }

    @PutMapping("/{id}/operativa")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Reactivar maquinaria tras mantenimiento (Rol: Proveedor)")
    public ResponseEntity<ToolResponse> finalizarMantenimiento(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return toolController.finalizarMantenimiento(id, authentication);
    }

    @GetMapping("/{id}/calificaciones")
    @Operation(summary = "Consultar calificaciones y promedio de una herramienta")
    public ResponseEntity<Map<String, Object>> getCalificaciones(@PathVariable Long id) {
        return toolController.getToolReviews(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalle de una herramienta por ID")
    public ResponseEntity<ToolResponse> getHerramientaById(@PathVariable Long id) {
        return toolController.getById(id);
    }
}
