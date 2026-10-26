package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.RentalTimelineEvent;
import com.rentatools.domain.tool.ToolRepository;

import java.time.LocalDate;

public class ExtendRentalBookingUseCase {
    private final RentalRepository rentalRepository;
    private final ToolRepository toolRepository;

    public ExtendRentalBookingUseCase(RentalRepository rentalRepository, ToolRepository toolRepository) {
        this.rentalRepository = rentalRepository;
        this.toolRepository = toolRepository;
    }

    public RentalResponse execute(Long rentalId, LocalDate newEndDate, String notes, String user) {
        RentalBooking rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId));

        LocalDate currentEnd = rental.getDateRange().endDate();
        if (newEndDate == null || !newEndDate.isAfter(currentEnd)) {
            throw new BusinessRuleValidationException("La nueva fecha final (" + newEndDate + ") debe ser estrictamente posterior a la fecha final actual (" + currentEnd + ").");
        }

        // Validar no solapamiento en el rango de extensión: [currentEnd + 1 día, newEndDate]
        LocalDate extensionStart = currentEnd.plusDays(1);
        boolean hasOverlap = rentalRepository.hasOverlappingBookings(rental.getToolId(), extensionStart, newEndDate);
        if (hasOverlap) {
            throw new BusinessRuleValidationException("No es posible extender: La herramienta ya tiene otra reserva comprometida en el periodo solicitado (" + extensionStart + " a " + newEndDate + ").");
        }

        RentalBooking extended = rental.extendTo(newEndDate, notes);
        RentalBooking saved = rentalRepository.save(extended);

        rentalRepository.recordTimelineEvent(RentalTimelineEvent.create(
                saved.getId(),
                rental.getStatus().name(),
                saved.getStatus().name(),
                user != null ? user : "Cliente",
                "Alquiler extendido hasta " + newEndDate + (notes != null && !notes.isBlank() ? ". Motivo: " + notes : "")
        ));

        return RentalResponse.fromDomain(saved);
    }
}
