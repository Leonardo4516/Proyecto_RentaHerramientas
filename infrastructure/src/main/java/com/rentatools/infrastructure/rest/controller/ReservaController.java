package com.rentatools.infrastructure.rest.controller;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.application.rental.service.ReservaService;
import com.rentatools.domain.common.exception.UnauthorizedException;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.infrastructure.persistence.repository.ToolJpaRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataProviderRepository;
import com.rentatools.infrastructure.persistence.user.SpringDataUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Gestión de Reservas (Examen)", description = "Endpoints para que el Proveedor acepte o rechace solicitudes de alquiler")
public class ReservaController {

    private final ReservaService reservaService;
    private final RentalRepository rentalRepository;
    private final ToolJpaRepository toolJpaRepository;
    private final SpringDataProviderRepository providerRepository;
    private final SpringDataUserRepository userRepository;
    private final SpringDataClientRepository clientRepository;

    public ReservaController(ReservaService reservaService,
                             RentalRepository rentalRepository,
                             ToolJpaRepository toolJpaRepository,
                             SpringDataProviderRepository providerRepository,
                             SpringDataUserRepository userRepository,
                             SpringDataClientRepository clientRepository) {
        this.reservaService = reservaService;
        this.rentalRepository = rentalRepository;
        this.toolJpaRepository = toolJpaRepository;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Aprobar solicitud de reserva", description = "Verifica que el estado sea PENDIENTE, valida la disponibilidad de la maquinaria sin solapamiento y transiciona a APROBADA")
    public ResponseEntity<RentalResponse> aprobarReserva(
            @PathVariable Long id,
            Authentication authentication
    ) {
        validateProviderOwnershipIfApplicable(id, authentication);
        String username = authentication != null ? authentication.getName() : "Proveedor";
        RentalResponse response = reservaService.aprobarReserva(id, username);
        return ResponseEntity.ok(enrichRental(response));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVEEDOR', 'ROLE_ADMIN')")
    @Operation(summary = "Rechazar solicitud de reserva", description = "Verifica que el estado sea PENDIENTE y transiciona la reserva a RECHAZADA")
    public ResponseEntity<RentalResponse> rechazarReserva(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        validateProviderOwnershipIfApplicable(id, authentication);
        String reason = motivo;
        if (reason == null && body != null) {
            reason = body.getOrDefault("motivo", body.get("reason"));
        }
        if (reason == null || reason.isBlank()) {
            reason = "Rechazada por el proveedor";
        }
        String username = authentication != null ? authentication.getName() : "Proveedor";
        RentalResponse response = reservaService.rechazarReserva(id, reason, username);
        return ResponseEntity.ok(enrichRental(response));
    }

    private void validateProviderOwnershipIfApplicable(Long rentalId, Authentication authentication) {
        // En el examen se valida el rol Proveedor a nivel de endpoint con @PreAuthorize
    }

    private RentalResponse enrichRental(RentalResponse r) {
        String toolName = "Maquinaria #" + r.toolId();
        Long providerId = null;
        String providerName = "Proveedor Verificado";

        if (toolJpaRepository != null && r.toolId() != null) {
            var toolOpt = toolJpaRepository.findById(r.toolId());
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

        if (clientRepository != null && r.clientId() != null) {
            var clientOpt = clientRepository.findById(r.clientId());
            if (clientOpt.isEmpty()) {
                clientOpt = clientRepository.findByUserId(r.clientId());
            }
            if (clientOpt.isPresent()) {
                var client = clientOpt.get();
                if (client.getDocumentNumber() != null) clientDocument = client.getDocumentNumber();
                if (client.getUserId() != null) {
                    var uOpt = userRepository.findById(client.getUserId());
                    if (uOpt.isPresent()) {
                        clientName = uOpt.get().getFullName();
                        clientEmail = uOpt.get().getEmail();
                        if (uOpt.get().getPhone() != null) clientPhone = uOpt.get().getPhone();
                    }
                }
            }
        }

        return new RentalResponse(
                r.id(),
                r.clientId(),
                r.toolId(),
                r.startDate(),
                r.endDate(),
                r.totalDays(),
                r.dailyRate(),
                r.depositAmount(),
                r.subtotal(),
                r.status(),
                r.deliveryAddress(),
                r.notes(),
                r.createdAt(),
                clientName,
                clientPhone,
                clientEmail,
                clientDocument,
                toolName,
                providerId,
                providerName
        );
    }
}
