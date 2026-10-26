package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.rental.dto.AvailabilityCheckResponse;
import com.rentatools.application.rental.dto.CreateRentalCommand;
import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.usecase.ChangeRentalStatusUseCase;
import com.rentatools.application.rental.usecase.CheckAvailabilityUseCase;
import com.rentatools.application.rental.usecase.CreateRentalBookingUseCase;
import com.rentatools.application.rental.usecase.ExtendRentalBookingUseCase;
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
    private final ExtendRentalBookingUseCase extendRentalBookingUseCase;
    private final com.rentatools.application.rental.usecase.RegisterReturnInspectionUseCase registerReturnInspectionUseCase;
    private final com.rentatools.application.rental.usecase.GetReturnInspectionUseCase getReturnInspectionUseCase;
    private final RentalRepository rentalRepository;
    private final SpringDataClientRepository clientRepository;
    private final SpringDataUserRepository userRepository;
    private final ToolJpaRepository toolJpaRepository;
    private final SpringDataProviderRepository providerRepository;
    private final com.rentatools.application.rental.service.ReservaService reservaService;
    private final com.rentatools.infrastructure.persistence.repository.RentalReviewJpaRepository reviewRepository;

    public RentalController(CreateRentalBookingUseCase createRentalBookingUseCase,
                            CheckAvailabilityUseCase checkAvailabilityUseCase,
                            ChangeRentalStatusUseCase changeRentalStatusUseCase,
                            ExtendRentalBookingUseCase extendRentalBookingUseCase,
                            com.rentatools.application.rental.usecase.RegisterReturnInspectionUseCase registerReturnInspectionUseCase,
                            com.rentatools.application.rental.usecase.GetReturnInspectionUseCase getReturnInspectionUseCase,
                            RentalRepository rentalRepository,
                            SpringDataClientRepository clientRepository,
                            SpringDataUserRepository userRepository,
                            ToolJpaRepository toolJpaRepository,
                            SpringDataProviderRepository providerRepository,
                            com.rentatools.application.rental.service.ReservaService reservaService,
                            com.rentatools.infrastructure.persistence.repository.RentalReviewJpaRepository reviewRepository) {
        this.createRentalBookingUseCase = createRentalBookingUseCase;
        this.checkAvailabilityUseCase = checkAvailabilityUseCase;
        this.changeRentalStatusUseCase = changeRentalStatusUseCase;
        this.extendRentalBookingUseCase = extendRentalBookingUseCase;
        this.registerReturnInspectionUseCase = registerReturnInspectionUseCase;
        this.getReturnInspectionUseCase = getReturnInspectionUseCase;
        this.rentalRepository = rentalRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.toolJpaRepository = toolJpaRepository;
        this.providerRepository = providerRepository;
        this.reservaService = reservaService;
        this.reviewRepository = reviewRepository;
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
    @Operation(summary = "Listar todas las reservas", description = "Retorna el historial de alquileres con aislamiento automático de privacidad para clientes y proveedores, con filtros opcionales por estado y rango de fechas")
    public ResponseEntity<List<RentalResponse>> getAllRentals(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
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

        if (status != null) {
            bookings = bookings.stream().filter(r -> r.getStatus() == status).toList();
        }
        if (startDate != null) {
            bookings = bookings.stream().filter(r -> r.getDateRange() != null && !r.getDateRange().startDate().isBefore(startDate)).toList();
        }
        if (endDate != null) {
            bookings = bookings.stream().filter(r -> r.getDateRange() != null && !r.getDateRange().endDate().isAfter(endDate)).toList();
        }

        List<RentalResponse> rentals = bookings.stream()
                .map(this::enrichRental)
                .toList();
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/my-rentals")
    @Operation(summary = "Listar reservas exclusivas del usuario autenticado (Cliente o Proveedor)")
    public ResponseEntity<List<RentalResponse>> getMyRentals(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        return getAllRentals(null, null, status, startDate, endDate, authentication);
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
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN', 'ROLE_CLIENTE')")
    @Operation(summary = "Cambiar estado de una reserva (Aprobar, Rechazar, Iniciar, Cancelar)")
    public ResponseEntity<RentalResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) BookingStatus newStatus,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) java.util.Map<String, String> body,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            boolean isClient = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
            if (isClient || isRentalClient(id, authentication.getName())) {
                validateClientOwnership(id, authentication.getName());
            } else {
                validateProviderOwnership(id, authentication.getName());
            }
        }

        BookingStatus targetStatus = newStatus;
        if (targetStatus == null && body != null && body.containsKey("status")) {
            targetStatus = BookingStatus.fromString(body.get("status"));
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

    @PostMapping({"/{id}/approve", "/{id}/aprobar"})
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Aprobar solicitud de reserva", description = "Verifica que el estado sea PENDIENTE, valida la disponibilidad de la maquinaria y transiciona a APROBADA")
    public ResponseEntity<RentalResponse> approveRental(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateProviderOwnership(id, authentication.getName());
        }
        String changedBy = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Proveedor";
        RentalResponse response = reservaService.aprobarReserva(id, changedBy);
        var refreshed = rentalRepository.findById(response.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(response));
    }

    public ResponseEntity<RentalResponse> aprobarReserva(Long id, Authentication authentication) {
        return approveRental(id, authentication);
    }

    @PostMapping({"/{id}/reject", "/{id}/rechazar"})
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Rechazar solicitud de reserva", description = "Verifica que el estado sea PENDIENTE y transiciona la reserva a RECHAZADA")
    public ResponseEntity<RentalResponse> rejectRental(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String motivo,
            @RequestBody(required = false) java.util.Map<String, String> body,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateProviderOwnership(id, authentication.getName());
        }
        String effectiveReason = reason != null ? reason : motivo;
        if (effectiveReason == null && body != null) {
            effectiveReason = body.getOrDefault("reason", body.get("motivo"));
        }
        if (effectiveReason == null || effectiveReason.isBlank()) {
            effectiveReason = "Rechazada por el proveedor";
        }
        String changedBy = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Proveedor";
        RentalResponse response = reservaService.rechazarReserva(id, effectiveReason, changedBy);
        var refreshed = rentalRepository.findById(response.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(response));
    }

    public ResponseEntity<RentalResponse> rechazarReserva(Long id, String motivo, java.util.Map<String, String> body, Authentication authentication) {
        return rejectRental(id, null, motivo, body, authentication);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Cancelar reserva de alquiler (alias POST /cancelar)")
    public ResponseEntity<RentalResponse> cancelarReservaPost(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelada por el usuario") String motivo,
            Authentication authentication
    ) {
        return cancelRental(id, motivo, authentication);
    }

    @PostMapping("/{id}/devolver")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Registrar devolución de maquinaria e inspección de daños (alias POST /devolver)")
    public ResponseEntity<RentalResponse> devolverReservaPost(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> returnData,
            Authentication authentication
    ) {
        return returnRental(id, returnData, authentication);
    }

    @PostMapping("/{id}/calificar")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
    @Operation(summary = "Calificar un alquiler completado (1 a 5 estrellas)", description = "Permite al cliente calificar y dejar una reseña para un alquiler finalizado")
    public ResponseEntity<com.rentatools.infrastructure.persistence.entity.RentalReviewEntity> calificarReserva(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> reviewData,
            Authentication authentication
    ) {
        RentalBooking rental = rentalRepository.findById(id)
                .orElseThrow(() -> new com.rentatools.domain.common.EntityNotFoundException("Reserva no encontrada con ID: " + id));

        if (rental.getStatus() != BookingStatus.COMPLETADA) {
            throw new com.rentatools.domain.common.BusinessRuleValidationException("Solo se pueden calificar alquileres que hayan sido COMPLETADOS.");
        }

        if (reviewRepository != null && reviewRepository.existsByRentalId(id)) {
            throw new com.rentatools.domain.common.BusinessRuleValidationException("Esta reserva ya ha sido calificada previamente.");
        }

        int score = 5;
        if (reviewData != null && reviewData.containsKey("score")) {
            score = Integer.parseInt(reviewData.get("score").toString());
        } else if (reviewData != null && reviewData.containsKey("puntuacion")) {
            score = Integer.parseInt(reviewData.get("puntuacion").toString());
        }

        if (score < 1 || score > 5) {
            throw new com.rentatools.domain.common.BusinessRuleValidationException("La puntuación debe estar comprendida entre 1 y 5 estrellas.");
        }

        String comment = (reviewData != null && reviewData.containsKey("comment"))
                ? reviewData.get("comment").toString()
                : (reviewData != null && reviewData.containsKey("comentario") ? reviewData.get("comentario").toString() : "");

        com.rentatools.infrastructure.persistence.entity.RentalReviewEntity review =
                new com.rentatools.infrastructure.persistence.entity.RentalReviewEntity(id, rental.getToolId(), rental.getClientId(), score, comment);

        com.rentatools.infrastructure.persistence.entity.RentalReviewEntity saved =
                reviewRepository != null ? reviewRepository.save(review) : review;

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}/calificacion")
    @Operation(summary = "Consultar calificación de una reserva")
    public ResponseEntity<com.rentatools.infrastructure.persistence.entity.RentalReviewEntity> getCalificacionReserva(@PathVariable Long id) {
        if (reviewRepository == null) {
            return ResponseEntity.notFound().build();
        }
        return reviewRepository.findByRentalId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

    @PostMapping("/{id}/extend")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Extender plazo de alquiler", description = "Extiende la fecha de finalización de una reserva activa validando atómicamente la disponibilidad de la maquinaria sin solapamiento")
    public ResponseEntity<RentalResponse> extendRental(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate,
            @RequestParam(required = false) String notes,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            boolean isClient = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
            if (isClient) {
                validateClientOwnership(id, authentication.getName());
            } else {
                validateProviderOwnership(id, authentication.getName());
            }
        }
        String user = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Cliente";
        RentalResponse response = extendRentalBookingUseCase.execute(id, newEndDate, notes, user);
        var refreshed = rentalRepository.findById(response.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Cancelar reserva de alquiler", description = "Cancela una reserva en estado PENDIENTE o CONFIRMADA y libera las fechas en el calendario")
    public ResponseEntity<RentalResponse> cancelRental(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            boolean isClient = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
            if (isClient) {
                validateClientOwnership(id, authentication.getName());
            } else {
                validateProviderOwnership(id, authentication.getName());
            }
        }
        String user = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Usuario";
        RentalResponse response = changeRentalStatusUseCase.execute(id, BookingStatus.CANCELADA, reason != null ? reason : "Reserva cancelada por el usuario", user);
        var refreshed = rentalRepository.findById(response.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(response));
    }

    @PutMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Despachar maquinaria a obra (Iniciar alquiler)", description = "Transiciona el estado de CONFIRMADA a EN_CURSO y registra la salida a obra")
    public ResponseEntity<RentalResponse> dispatchRental(
            @PathVariable Long id,
            @RequestParam(required = false) String transportGuide,
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) String notes,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateProviderOwnership(id, authentication.getName());
        }
        String user = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Proveedor";
        StringBuilder dispatchInfo = new StringBuilder("Despacho a obra confirmado.");
        if (transportGuide != null && !transportGuide.isBlank()) dispatchInfo.append(" Guía Transporte: ").append(transportGuide);
        if (driverName != null && !driverName.isBlank()) dispatchInfo.append(" Conductor: ").append(driverName);
        if (notes != null && !notes.isBlank()) dispatchInfo.append(" Notas: ").append(notes);

        RentalResponse response = changeRentalStatusUseCase.execute(id, BookingStatus.EN_CURSO, dispatchInfo.toString(), user);
        var refreshed = rentalRepository.findById(response.id());
        return ResponseEntity.ok(refreshed.map(this::enrichRental).orElse(response));
    }

    @PostMapping("/{id}/inspection-return")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Registrar devolución física e inspección técnica", description = "Liquida penalizaciones por daños, calcula el depósito a reembolsar y devuelve la maquinaria a estado DISPONIBLE")
    public ResponseEntity<com.rentatools.application.rental.dto.ReturnInspectionResponse> registerInspectionReturn(
            @PathVariable Long id,
            @RequestBody(required = false) com.rentatools.application.rental.dto.RegisterReturnInspectionCommand command,
            Authentication authentication
    ) {
        if (authentication != null && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            validateProviderOwnership(id, authentication.getName());
        }
        String user = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "Inspector Técnico";
        var cmd = command != null ? command : new com.rentatools.application.rental.dto.RegisterReturnInspectionCommand(
                1L, "EXCELENTE", "Devolución conforme en patio", java.util.List.of(), true
        );
        var response = registerReturnInspectionUseCase.execute(id, cmd, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/return-inspection")
    @Operation(summary = "Consultar acta de inspección técnica", description = "Retorna el informe detallado de devolución, reporte de daños y liquidación del depósito de garantía")
    public ResponseEntity<com.rentatools.application.rental.dto.ReturnInspectionResponse> getReturnInspection(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(getReturnInspectionUseCase.execute(id));
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

    private void validateClientOwnership(Long rentalId, String username) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Usuario autenticado no válido.");
        }
        var rentalOpt = rentalRepository.findById(rentalId);
        if (rentalOpt.isEmpty()) {
            throw new com.rentatools.domain.common.EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId);
        }
        Long userId = userOpt.get().getId();
        Long rentalClientId = rentalOpt.get().getClientId();

        var clientOpt = clientRepository.findByUserId(userId);
        boolean isOwner = clientOpt.isPresent() && clientOpt.get().getId().equals(rentalClientId);
        if (!isOwner && !userId.equals(rentalClientId)) {
            throw new UnauthorizedException("No tienes autorización para gestionar reservas de otro cliente.");
        }
    }

    private boolean isRentalClient(Long rentalId, String username) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;
        var rentalOpt = rentalRepository.findById(rentalId);
        if (rentalOpt.isEmpty()) return false;
        Long userId = userOpt.get().getId();
        Long rentalClientId = rentalOpt.get().getClientId();
        if (userId.equals(rentalClientId)) return true;
        var clientOpt = clientRepository.findByUserId(userId);
        return clientOpt.isPresent() && clientOpt.get().getId().equals(rentalClientId);
    }
}
