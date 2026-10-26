package com.rentatools.domain.rental;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface RentalRepository {
    RentalBooking save(RentalBooking rental);
    Optional<RentalBooking> findById(Long id);
    List<RentalBooking> findByClientId(Long clientId);
    List<RentalBooking> findByToolId(Long toolId);
    List<RentalBooking> findActiveRentalsByToolId(Long toolId);
    List<RentalBooking> findAll();
    
    /**
     * Verifica si existen reservas activas que se solapen con el rango solicitado.
     */
    boolean hasOverlappingBookings(Long toolId, LocalDate startDate, LocalDate endDate);

    default boolean hasOverlappingBookingsExcluding(Long toolId, LocalDate startDate, LocalDate endDate, Long excludeRentalId) {
        if (excludeRentalId == null) {
            return hasOverlappingBookings(toolId, startDate, endDate);
        }
        DateRange requested = new DateRange(startDate, endDate);
        return findActiveRentalsByToolId(toolId).stream()
                .filter(r -> r.getId() != null && !r.getId().equals(excludeRentalId))
                .filter(r -> r.getStatus() == BookingStatus.APROBADA || r.getStatus() == BookingStatus.CONFIRMADA || r.getStatus() == BookingStatus.EN_CURSO)
                .anyMatch(r -> r.getDateRange().overlapsWith(requested));
    }

    /**
     * Verifica si existen otras reservas confirmadas o aprobadas que se solapen con el rango, excluyendo una reserva específica.
     */
    default boolean hasOtherOverlappingConfirmedBooking(Long toolId, Long rentalId, LocalDate startDate, LocalDate endDate) {
        return hasOverlappingBookingsExcluding(toolId, startDate, endDate, rentalId);
    }
    }

    default void recordTimelineEvent(RentalTimelineEvent event) {}

    default List<RentalTimelineEvent> findTimelineByRentalId(Long rentalId) {
        return List.of();
    }

    default ReturnInspection saveInspection(Long rentalId, ReturnInspection inspection) {
        return inspection;
    }

    default Optional<ReturnInspection> findInspectionByRentalId(Long rentalId) {
        return Optional.empty();
    }
}

