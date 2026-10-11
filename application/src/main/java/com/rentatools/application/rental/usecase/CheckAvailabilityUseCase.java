package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.AvailabilityCheckResponse;
import com.rentatools.domain.rental.RentalRepository;
import java.time.LocalDate;
import java.util.UUID;

public class CheckAvailabilityUseCase {
    private final RentalRepository rentalRepository;

    public CheckAvailabilityUseCase(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public AvailabilityCheckResponse execute(UUID toolId, LocalDate startDate, LocalDate endDate) {
        boolean hasOverlap = rentalRepository.hasOverlappingBookings(toolId, startDate, endDate);
        boolean isAvailable = !hasOverlap;
        String msg = isAvailable
                ? "La herramienta se encuentra 100% disponible para el rango de fechas solicitado."
                : "Conflicto: Ya existe una reserva activa para esta herramienta en el rango de fechas seleccionado.";
        return new AvailabilityCheckResponse(toolId, startDate, endDate, isAvailable, msg);
    }
}
