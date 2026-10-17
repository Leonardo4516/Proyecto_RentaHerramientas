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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.rentatools.domain.common.exception.UnauthorizedException;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Operation(summary = "Crear solicitud de reserva de alquiler", description = "Valida que no existan reservas activas en ese rango de fechas de forma atómica y concurrente")
    public ResponseEntity<RentalResponse> createRental(
            @Valid @RequestBody CreateRentalCommand command,
            Authentication authentication
    ) {
        CreateRentalCommand effectiveCommand = command;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            boolean isClient = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
            if (isClient && (command.clientId() == null || command.clientId() <= 0)) {
                var userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    var clientOpt = clientRepository.findByUserId(userOpt.get().getId());
                    Long resolvedClientId = clientOpt.map(c -> c.getId()).orElse(userOpt.get().getId());
                    effectiveCommand = new CreateRentalCommand(
                            resolvedClientId,
                            command.toolId(),
                            command.startDate(),
                            command.endDate(),
                            command.deliveryAddress(),
                            command.notes()
                    );
                }
            }
        }
        RentalResponse response = createRentalBookingUseCase.execute(effectiveCommand);
        var createdBooking = rentalRepository.findById(response.id());
        RentalResponse finalResponse = createdBooking.map(this::enrichRental).orElse(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(finalResponse);
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
    @Operation(summary = "Listar todas las reservas", description = "Retorna el historial de alquileres con aislamiento automático de privacidad para clientes y proveedores")
    public ResponseEntity<List<RentalResponse>> getAllRentals(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long clientId,
            Authentication authentication
    ) {
        List<RentalBooking> bookings;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isProvider = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROVEEDOR"));
            boolean isClient = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (isAdmin) {
                if (providerId != null) {
                    var toolIds = toolJpaRepository.findByProviderId(providerId).stream().map(t -> t.getId()).toList();
                    bookings = rentalRepository.findAll().stream().filter(r -> toolIds.contains(r.getToolId())).toList();
                } else if (clientId != null) {
                    bookings = rentalRepository.findByClientId(clientId);
                } else {
                    bookings = rentalRepository.findAll();
                }
            } else if (isProvider) {
                // Proveedor: Solo ve alquileres de maquinaria que le pertenece
                var userOpt = userRepository.findByUsername(username);
                Long resolvedProviderId = null;
                if (userOpt.isPresent()) {
                    var provOpt = providerRepository.findByUserId(userOpt.get().getId());
                    if (provOpt.isPresent()) {
                        resolvedProviderId = provOpt.get().getId();
                    }
                }
                if (resolvedProviderId != null) {
                    var toolIds = toolJpaRepository.findByProviderId(resolvedProviderId).stream().map(t -> t.getId()).toList();
                    bookings = rentalRepository.findAll().stream().filter(r -> toolIds.contains(r.getToolId())).toList();
                } else {
                    bookings = List.of();
                }
            } else if (isClient) {
                // Cliente: Solo ve sus propios alquileres contratados
                var userOpt = userRepository.findByUsername(username);
                Long resolvedClientId = null;
                if (userOpt.isPresent()) {
                    var clientOpt = clientRepository.findByUserId(userOpt.get().getId());
                    resolvedClientId = clientOpt.map(c -> c.getId()).orElse(userOpt.get().getId());
                }
                if (resolvedClientId != null) {
                    bookings = rentalRepository.findByClientId(resolvedClientId);
                } else {
                    bookings = List.of();
                }
            } else {
                bookings = rentalRepository.findAll();
            }
        } else {
            bookings = rentalRepository.findAll();
        }

        List<RentalResponse> rentals = bookings.stream()
                .map(this::enrichRental)
                .toList();
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/my-rentals")
    @Operation(summary = "Listar reservas exclusivas del usuario autenticado (Cliente o Proveedor)")
    public ResponseEntity<List<RentalResponse>> getMyRentals(Authentication authentication) {
        return getAllRentals(null, null, authentication);
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
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Cambiar estado de una reserva (Aprobar, Rechazar, Iniciar, Cancelar)")
    public ResponseEntity<RentalResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) BookingStatus newStatus,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) java.util.Map<String, String> body,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateProviderOwnership(id, authentication.getName());
        }

        BookingStatus targetStatus = newStatus;
        if (targetStatus == null && body != null && body.containsKey("status")) {
            targetStatus = BookingStatus.valueOf(body.get("status").toUpperCase());
        }
        if (targetStatus == null) {
            targetStatus = BookingStatus.CONFIRMADA;
        }
        String changedBy = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "Proveedor";
        RentalResponse updated = changeRentalStatusUseCase.execute(id, targetStatus, reason, changedBy);
        var refreshed = rentalRepository.findById(updated.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(updated));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Registrar devolución de maquinaria e inspección de daños")
    public ResponseEntity<RentalResponse> returnRental(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> returnData,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateProviderOwnership(id, authentication.getName());
        }

        String changedBy = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "Proveedor";
        RentalResponse updated = changeRentalStatusUseCase.execute(id, BookingStatus.COMPLETADA, "Devolución e inspección registrada", changedBy);
        var refreshed = rentalRepository.findById(updated.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(updated));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Consultar línea de tiempo y auditoría de eventos de una reserva")
    public ResponseEntity<List<com.rentatools.domain.rental.RentalTimelineEvent>> getRentalTimeline(
            @PathVariable Long id,
            Authentication authentication
    ) {
        var rentalOpt = rentalRepository.findById(id);
        if (rentalOpt.isEmpty()) {
            throw new com.rentatools.domain.common.EntityNotFoundException("Alquiler no encontrado con ID: " + id);
        }
        var rental = rentalOpt.get();

        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            boolean isProvider = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROVEEDOR"));
            if (isProvider) {
                validateProviderOwnership(id, authentication.getName());
            }
        }

        List<com.rentatools.domain.rental.RentalTimelineEvent> timeline = rentalRepository.findTimelineByRentalId(id);
        if (timeline == null || timeline.isEmpty()) {
            timeline = generateFallbackTimeline(rental);
        }
        return ResponseEntity.ok(timeline);
    }

    private List<com.rentatools.domain.rental.RentalTimelineEvent> generateFallbackTimeline(RentalBooking r) {
        List<com.rentatools.domain.rental.RentalTimelineEvent> list = new java.util.ArrayList<>();
        java.time.Instant created = r.getCreatedAt() != null ? r.getCreatedAt() : java.time.Instant.now();

        list.add(new com.rentatools.domain.rental.RentalTimelineEvent(
                1L, r.getId(), null, "PENDIENTE", "Cliente #" + r.getClientId(),
                "Solicitud de reserva radicada en la plataforma", created
        ));

        if (r.getStatus() == BookingStatus.CONFIRMADA || r.getStatus() == BookingStatus.EN_CURSO || r.getStatus() == BookingStatus.COMPLETADA) {
            list.add(new com.rentatools.domain.rental.RentalTimelineEvent(
                    2L, r.getId(), "PENDIENTE", "CONFIRMADA", "Proveedor",
                    "Reserva aprobada y disponibilidad confirmada", created.plusSeconds(3600)
            ));
        }

        if (r.getStatus() == BookingStatus.EN_CURSO || r.getStatus() == BookingStatus.COMPLETADA) {
            list.add(new com.rentatools.domain.rental.RentalTimelineEvent(
                    3L, r.getId(), "CONFIRMADA", "EN_CURSO", "Proveedor",
                    "Maquinaria despachada a obra del cliente", created.plusSeconds(7200)
            ));
        }

        if (r.getStatus() == BookingStatus.COMPLETADA) {
            list.add(new com.rentatools.domain.rental.RentalTimelineEvent(
                    4L, r.getId(), "EN_CURSO", "COMPLETADA", "Proveedor",
                    "Devolución recibida e inspección completada sin novedades", created.plusSeconds(14400)
            ));
        }

        if (r.getStatus() == BookingStatus.RECHAZADA) {
            list.add(new com.rentatools.domain.rental.RentalTimelineEvent(
                    2L, r.getId(), "PENDIENTE", "RECHAZADA", "Proveedor",
                    r.getNotes() != null ? r.getNotes() : "Solicitud rechazada por el proveedor", created.plusSeconds(3600)
            ));
        }

        if (r.getStatus() == BookingStatus.CANCELADA) {
            list.add(new com.rentatools.domain.rental.RentalTimelineEvent(
                    2L, r.getId(), "PENDIENTE", "CANCELADA", "Cliente #" + r.getClientId(),
                    "Reserva cancelada por el usuario", created.plusSeconds(1800)
            ));
        }

        return list;
    }

    private void validateProviderOwnership(Long rentalId, String username) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Usuario autenticado no válido.");
        }
        var provOpt = providerRepository.findByUserId(userOpt.get().getId());
        if (provOpt.isEmpty()) {
            throw new UnauthorizedException("El usuario no tiene un perfil de proveedor asociado.");
        }
        Long myProviderId = provOpt.get().getId();

        var rentalOpt = rentalRepository.findById(rentalId);
        if (rentalOpt.isEmpty()) {
            throw new com.rentatools.domain.common.EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId);
        }

        var toolOpt = toolJpaRepository.findById(rentalOpt.get().getToolId());
        if (toolOpt.isPresent() && !myProviderId.equals(toolOpt.get().getProviderId())) {
            throw new UnauthorizedException("No tienes autorización para gestionar reservas de maquinaria perteneciente a otra empresa.");
        }
    }
}
