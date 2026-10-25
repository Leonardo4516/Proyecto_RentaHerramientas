package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.infrastructure.persistence.entity.RentalReviewEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas (Alias Examen en Español)", description = "Controlador alias para compatibilidad exacta con enunciados y colecciones de prueba de evaluación técnica")
public class ReservaController {

    private final RentalController rentalController;

    public ReservaController(RentalController rentalController) {
        this.rentalController = rentalController;
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Aprobar reserva (Rol: Proveedor)")
    public ResponseEntity<RentalResponse> aprobarReserva(@PathVariable Long id, Authentication authentication) {
        return rentalController.aprobarReserva(id, authentication);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Rechazar reserva (Rol: Proveedor)")
    public ResponseEntity<RentalResponse> rechazarReserva(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Rechazada por el proveedor") String motivo,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        return rentalController.rechazarReserva(id, motivo, body, authentication);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Cancelar reserva (Rol: Cliente)")
    public ResponseEntity<RentalResponse> cancelarReserva(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelada por el cliente") String motivo,
            Authentication authentication) {
        return rentalController.cancelarReservaPost(id, motivo, authentication);
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Devolver maquinaria e inspección de daños (Rol: Proveedor)")
    public ResponseEntity<RentalResponse> devolverReserva(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> returnData,
            Authentication authentication) {
        return rentalController.devolverReservaPost(id, returnData, authentication);
    }

    @PostMapping("/{id}/extender")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Extender plazo de reserva (Rol: Cliente)")
    public ResponseEntity<RentalResponse> extenderReserva(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nuevaFechaFin,
            @RequestParam(required = false) String motivo,
            Authentication authentication) {
        return rentalController.extendRental(id, nuevaFechaFin, motivo, authentication);
    }

    @PostMapping("/{id}/calificar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @Operation(summary = "Calificar reserva completada (Rol: Cliente)")
    public ResponseEntity<RentalReviewEntity> calificarReserva(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> reviewData,
            Authentication authentication) {
        return rentalController.calificarReserva(id, reviewData, authentication);
    }

    @GetMapping("/{id}/calificacion")
    @Operation(summary = "Consultar calificación de una reserva")
    public ResponseEntity<RentalReviewEntity> getCalificacionReserva(@PathVariable Long id) {
        return rentalController.getCalificacionReserva(id);
    }
}
