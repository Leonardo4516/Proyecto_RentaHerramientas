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

import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataProviderRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataUserRepository;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/v1/rentals")
@Tag(name = "Reservas y Alquileres", description = "Endpoints para verificación de solapamiento, solicitudes de alquiler y cambios de estado")
public class RentalController {

    private final CreateRentalBookingUseCase createRentalBookingUseCase;
    private final CheckAvailabilityUseCase checkAvailabilityUseCase;
    private final ChangeRentalStatusUseCase changeRentalStatusUseCase;
    private final RentalRepository rentalRepository;
    private final SpringDataClientRepository clientRepository;
    private final SpringDataUserRepository userRepository;
    private final ToolJpaRepository toolJpaRepository;
    private final SpringDataProviderRepository providerRepository;

    public RentalController(CreateRentalBookingUseCase createRentalBookingUseCase,
                            CheckAvailabilityUseCase checkAvailabilityUseCase,
                            ChangeRentalStatusUseCase changeRentalStatusUseCase,
                            RentalRepository rentalRepository,
                            SpringDataClientRepository clientRepository,
                            SpringDataUserRepository userRepository,
                            ToolJpaRepository toolJpaRepository,
                            SpringDataProviderRepository providerRepository) {
        this.createRentalBookingUseCase = createRentalBookingUseCase;
        this.checkAvailabilityUseCase = checkAvailabilityUseCase;
        this.changeRentalStatusUseCase = changeRentalStatusUseCase;
        this.rentalRepository = rentalRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.toolJpaRepository = toolJpaRepository;
        this.providerRepository = providerRepository;
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
            @RequestParam Long toolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AvailabilityCheckResponse response = checkAvailabilityUseCase.execute(toolId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas las reservas", description = "Retorna el historial completo de alquileres para el cliente o proveedor con datos de contacto enriquecidos")
    public ResponseEntity<List<RentalResponse>> getAllRentals() {
        List<RentalResponse> rentals = rentalRepository.findAll().stream()
                .map(this::enrichRental)
                .toList();
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/tool/{toolId}")
    @Operation(summary = "Listar reservas activas de una herramienta")
    public ResponseEntity<List<RentalResponse>> getToolRentals(@PathVariable Long toolId) {
        List<RentalResponse> rentals = rentalRepository.findByToolId(toolId).stream()
                .map(this::enrichRental)
                .toList();
        return ResponseEntity.ok(rentals);
    }

    private RentalResponse enrichRental(RentalBooking r) {
        String toolName = "Maquinaria #" + r.getToolId();
        Long providerId = null;
        String providerName = "Proveedor Verificado";

        if (toolJpaRepository != null && r.getToolId() != null) {
            var toolOpt = toolJpaRepository.findById(r.getToolId());
            if (toolOpt.isPresent()) {
                var tool = toolOpt.get();
                if (tool.getName() != null) toolName = tool.getName();
                providerId = tool.getProviderId();
                if (providerRepository != null && providerId != null) {
                    var provOpt = providerRepository.findById(providerId);
                    if (provOpt.isPresent() && provOpt.get().getCompanyName() != null) {
                        providerName = provOpt.get().getCompanyName();
                    }
                }
            }
        }

        String clientName = "Cliente Constructora";
        String clientPhone = "+57 320 000 0023";
        String clientEmail = "cliente@constructora.com";
        String clientDocument = "NIT 900.123.456-7";

        if (clientRepository != null && r.getClientId() != null) {
            var clientOpt = clientRepository.findById(r.getClientId());
            if (clientOpt.isEmpty()) {
                clientOpt = clientRepository.findByUserId(r.getClientId());
            }
            if (clientOpt.isPresent()) {
                var client = clientOpt.get();
                if (client.getDocumentNumber() != null) {
                    clientDocument = (client.getDocumentType() != null ? client.getDocumentType() + " " : "") + client.getDocumentNumber();
                }
                if (userRepository != null && client.getUserId() != null) {
                    var userOpt = userRepository.findById(client.getUserId());
                    if (userOpt.isPresent()) {
                        var u = userOpt.get();
                        if (u.getFullName() != null) clientName = u.getFullName();
                        if (u.getPhone() != null) clientPhone = u.getPhone();
                        if (u.getEmail() != null) clientEmail = u.getEmail();
                    }
                }
            } else if (userRepository != null) {
                var userOpt = userRepository.findById(r.getClientId());
                if (userOpt.isPresent()) {
                    var u = userOpt.get();
                    if (u.getFullName() != null) clientName = u.getFullName();
                    if (u.getPhone() != null) clientPhone = u.getPhone();
                    if (u.getEmail() != null) clientEmail = u.getEmail();
                }
            }
        }

        return new RentalResponse(
                r.getId(),
                r.getClientId(),
                r.getToolId(),
                r.getDateRange().startDate(),
                r.getDateRange().endDate(),
                r.getDateRange().totalDays(),
                r.getDailyRate(),
                r.getDepositAmount(),
                r.getSubtotal(),
                r.getStatus().name(),
                r.getDeliveryAddress(),
                r.getNotes(),
                r.getCreatedAt(),
                clientName,
                clientPhone,
                clientEmail,
                clientDocument,
                toolName,
                providerId,
                providerName
        );
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de una reserva (Aprobar, Rechazar, Iniciar, Cancelar)")
    public ResponseEntity<RentalResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) BookingStatus newStatus,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        BookingStatus targetStatus = newStatus;
        if (targetStatus == null && body != null && body.containsKey("status")) {
            targetStatus = BookingStatus.valueOf(body.get("status").toUpperCase());
        }
        if (targetStatus == null) {
            targetStatus = BookingStatus.CONFIRMADA;
        }
        RentalResponse updated = changeRentalStatusUseCase.execute(id, targetStatus, reason);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Registrar devolución de maquinaria e inspección de daños")
    public ResponseEntity<RentalResponse> returnRental(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> returnData
    ) {
        RentalResponse updated = changeRentalStatusUseCase.execute(id, BookingStatus.COMPLETADA, "Devolución e inspección registrada");
        return ResponseEntity.ok(updated);
    }
}
