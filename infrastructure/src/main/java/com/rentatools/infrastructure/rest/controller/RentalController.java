package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.rental.dto.AvailabilityCheckResponse;
import com.rentatools.application.rental.dto.CreateRentalCommand;
import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.usecase.ChangeRentalStatusUseCase;
import com.rentatools.application.rental.usecase.CheckAvailabilityUseCase;
import com.rentatools.application.rental.usecase.CreateRentalBookingUseCase;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.RentalRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rentals")
@Tag(name = "Reservas y Alquileres", description = "Endpoints para verificación de solapamiento, solicitudes de alquiler y cambios de estado")
public class RentalController {

    private final CreateRentalBookingUseCase createRentalBookingUseCase;
    private final CheckAvailabilityUseCase checkAvailabilityUseCase;
    private final ChangeRentalStatusUseCase changeRentalStatusUseCase;
    private final RentalRepository rentalRepository;

    public RentalController(CreateRentalBookingUseCase createRentalBookingUseCase,
                            CheckAvailabilityUseCase checkAvailabilityUseCase,
                            ChangeRentalStatusUseCase changeRentalStatusUseCase,
                            RentalRepository rentalRepository) {
        this.createRentalBookingUseCase = createRentalBookingUseCase;
        this.checkAvailabilityUseCase = checkAvailabilityUseCase;
        this.changeRentalStatusUseCase = changeRentalStatusUseCase;
        this.rentalRepository = rentalRepository;
    }

    @PostMapping
    @Operation(summary = "Crear solicitud de reserva de alquiler", description = "Valida que no existan reservas activas en ese rango de fechas")
    public ResponseEntity<RentalResponse> createRental(@Valid @RequestBody CreateRentalCommand command) {
        RentalResponse response = createRentalBookingUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Comprobar disponibilidad de una herramienta sin solapamiento de fechas")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @RequestParam UUID toolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AvailabilityCheckResponse response = checkAvailabilityUseCase.execute(toolId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tool/{toolId}")
    @Operation(summary = "Listar reservas activas de una herramienta")
    public ResponseEntity<List<RentalResponse>> getToolRentals(@PathVariable UUID toolId) {
        List<RentalResponse> rentals = rentalRepository.findByToolId(toolId).stream()
                .map(RentalResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(rentals);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de una reserva (Aprobar, Rechazar, Iniciar, Cancelar)")
    public ResponseEntity<RentalResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam BookingStatus newStatus,
            @RequestParam(required = false) String reason
    ) {
        RentalResponse updated = changeRentalStatusUseCase.execute(id, newStatus, reason);
        return ResponseEntity.ok(updated);
    }
}
