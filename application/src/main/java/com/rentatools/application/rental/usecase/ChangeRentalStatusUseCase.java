package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import java.util.UUID;

public class ChangeRentalStatusUseCase {
    private final RentalRepository rentalRepository;

    public ChangeRentalStatusUseCase(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public RentalResponse execute(UUID rentalId, BookingStatus newStatus, String reason) {
        RentalBooking rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId));

        switch (newStatus) {
            case CONFIRMADA -> rental.confirm();
            case RECHAZADA -> rental.reject(reason != null ? reason : "Rechazado por el proveedor.");
            case EN_CURSO -> rental.startRental();
            case CANCELADA -> rental.cancel();
            default -> {}
        }

        RentalBooking updated = rentalRepository.save(rental);
        return RentalResponse.fromDomain(updated);
    }
}
