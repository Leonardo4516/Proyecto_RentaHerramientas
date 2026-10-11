package com.rentatools.domain.rental;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalRepository {
    RentalBooking save(RentalBooking rental);
    Optional<RentalBooking> findById(UUID id);
    List<RentalBooking> findByClientId(UUID clientId);
    List<RentalBooking> findByToolId(UUID toolId);
    List<RentalBooking> findActiveRentalsByToolId(UUID toolId);
    
    /**
     * Verifica si existen reservas activas que se solapen con el rango solicitado.
     */
    boolean hasOverlappingBookings(UUID toolId, LocalDate startDate, LocalDate endDate);
}
