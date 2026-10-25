package com.rentatools.application.rental.service;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.RentalTimelineEvent;
import com.rentatools.domain.rental.exception.HerramientaNoDisponibleException;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;

public class ReservaService {

    private final RentalRepository rentalRepository;
    private final ToolRepository toolRepository;

    public ReservaService(RentalRepository rentalRepository, ToolRepository toolRepository) {
        this.rentalRepository = rentalRepository;
        this.toolRepository = toolRepository;
    }

    public RentalResponse aprobarReserva(Long idReserva) {
        return aprobarReserva(idReserva, "Proveedor");
    }

    public RentalResponse aprobarReserva(Long idReserva, String changedBy) {
        // 1. Buscar la reserva
        RentalBooking rental = rentalRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + idReserva));

        // 2. Verificar que el estado actual sea PENDIENTE
        if (rental.getStatus() != BookingStatus.PENDIENTE) {
            throw new BusinessRuleValidationException("Solo se pueden aprobar reservas en estado PENDIENTE. Estado actual: " + rental.getStatus());
        }

        // 3. Validar disponibilidad de la herramienta
        if (toolRepository != null) {
            Tool tool = toolRepository.findById(rental.getToolId())
                    .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + rental.getToolId()));

            if (tool.getStatus() == ToolStatus.EN_MANTENIMIENTO || tool.getStatus() == ToolStatus.DE_BAJA) {
                throw new HerramientaNoDisponibleException("La herramienta #" + tool.getId() + " no está disponible operacionalmente (" + tool.getStatus() + ").");
            }
        }

        boolean hasConflict = rentalRepository.hasOtherOverlappingConfirmedBooking(
                rental.getToolId(),
                rental.getId(),
                rental.getDateRange().startDate(),
                rental.getDateRange().endDate()
        );

        if (hasConflict) {
            throw new HerramientaNoDisponibleException("La herramienta ya se encuentra comprometida con otra reserva activa en el rango de fechas solicitado.");
        }

        // 4. Cambiar estado a APROBADA
        rental.approve();
        RentalBooking updated = rentalRepository.save(rental);

        rentalRepository.recordTimelineEvent(RentalTimelineEvent.create(
                idReserva,
                BookingStatus.PENDIENTE.name(),
                BookingStatus.APROBADA.name(),
                changedBy != null ? changedBy : "Proveedor",
                "Reserva aprobada por el proveedor"
        ));

        return RentalResponse.fromDomain(updated);
    }

    public RentalResponse rechazarReserva(Long idReserva) {
        return rechazarReserva(idReserva, "Rechazada por el proveedor", "Proveedor");
    }

    public RentalResponse rechazarReserva(Long idReserva, String motivo) {
        return rechazarReserva(idReserva, motivo, "Proveedor");
    }

    public RentalResponse rechazarReserva(Long idReserva, String motivo, String changedBy) {
        // 1. Buscar la reserva
        RentalBooking rental = rentalRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + idReserva));

        // 2. Verificar que el estado actual sea PENDIENTE
        if (rental.getStatus() != BookingStatus.PENDIENTE) {
            throw new BusinessRuleValidationException("Solo se pueden rechazar reservas en estado PENDIENTE. Estado actual: " + rental.getStatus());
        }

        // 3. Cambiar estado a RECHAZADA
        rental.reject(motivo != null ? motivo : "Rechazada por el proveedor");
        RentalBooking updated = rentalRepository.save(rental);

        rentalRepository.recordTimelineEvent(RentalTimelineEvent.create(
                idReserva,
                BookingStatus.PENDIENTE.name(),
                BookingStatus.RECHAZADA.name(),
                changedBy != null ? changedBy : "Proveedor",
                motivo != null ? motivo : "Reserva rechazada por el proveedor"
        ));

        return RentalResponse.fromDomain(updated);
    }
}
