package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.RentalTimelineEvent;

public class ChangeRentalStatusUseCase {
    private final RentalRepository rentalRepository;

    public ChangeRentalStatusUseCase(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public RentalResponse execute(Long rentalId, BookingStatus newStatus, String reason) {
        return execute(rentalId, newStatus, reason, "Sistema");
    }

    public RentalResponse execute(Long rentalId, BookingStatus newStatus, String reason, String changedBy) {
        RentalBooking rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId));

        BookingStatus previousStatus = rental.getStatus();

        switch (newStatus) {
            case APROBADA, CONFIRMADA -> {
                if (previousStatus != BookingStatus.PENDIENTE) {
                    throw new com.rentatools.domain.common.BusinessRuleValidationException("Solo se pueden aprobar reservas en estado PENDIENTE.");
                }
                boolean hasConflict = rentalRepository.hasOverlappingBookingsExcluding(
                        rental.getToolId(),
                        rental.getDateRange().startDate(),
                        rental.getDateRange().endDate(),
                        rental.getId()
                );
                if (hasConflict) {
                    throw new com.rentatools.domain.common.exception.HerramientaNoDisponibleException(
                            "La herramienta ya no tiene disponibilidad para las fechas solicitadas."
                    );
                }
                if (newStatus == BookingStatus.APROBADA) {
                    rental.approve();
                } else {
                    rental.confirm();
                }
            }
            case RECHAZADA -> rental.reject(reason != null ? reason : "Rechazado por el proveedor.");
            case EN_CURSO -> rental.startRental();
            case COMPLETADA -> rental.completeReturn(null);
            case CANCELADA -> rental.cancel();
            default -> {}
        }

        RentalBooking updated = rentalRepository.save(rental);

        rentalRepository.recordTimelineEvent(RentalTimelineEvent.create(
                rentalId,
                previousStatus != null ? previousStatus.name() : null,
                newStatus.name(),
                changedBy != null ? changedBy : "Sistema",
                reason != null ? reason : "Cambio de estado a " + newStatus
        ));

        return RentalResponse.fromDomain(updated);
    }
}
