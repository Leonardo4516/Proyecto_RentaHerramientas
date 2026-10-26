package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.CreateRentalCommand;
import com.rentatools.application.rental.dto.RentalResponse;
import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.tool.Tool;
import com.rentatools.domain.tool.ToolRepository;
import com.rentatools.domain.tool.ToolStatus;

public class CreateRentalBookingUseCase {
    private final RentalRepository rentalRepository;
    private final ToolRepository toolRepository;

    public CreateRentalBookingUseCase(RentalRepository rentalRepository, ToolRepository toolRepository) {
        this.rentalRepository = rentalRepository;
        this.toolRepository = toolRepository;
    }

    public RentalResponse execute(CreateRentalCommand command) {
        Tool tool = toolRepository.findByIdWithLock(command.toolId())
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + command.toolId()));

        if (tool.getStatus() == ToolStatus.EN_MANTENIMIENTO || tool.getStatus() == ToolStatus.DE_BAJA) {
            throw new BusinessRuleValidationException("La herramienta no se encuentra operativa. Estado: " + tool.getStatus());
        }

        DateRange requestedRange = new DateRange(command.startDate(), command.endDate());

        // Control de disponibilidad sin solapamiento
        boolean hasOverlap = rentalRepository.hasOverlappingBookings(
                tool.getId(), requestedRange.startDate(), requestedRange.endDate()
        );
        if (hasOverlap) {
            throw new BusinessRuleValidationException(
                    "No es posible reservar: La herramienta ya tiene un alquiler reservado en las fechas solicitadas."
            );
        }

        RentalBooking booking = RentalBooking.create(
                command.clientId(),
                tool.getId(),
                requestedRange,
                tool.getDailyPrice(),
                tool.getDepositAmount(),
                command.deliveryAddress(),
                command.notes()
        );

        RentalBooking saved = rentalRepository.save(booking);

        rentalRepository.recordTimelineEvent(com.rentatools.domain.rental.RentalTimelineEvent.create(
                saved.getId(),
                null,
                saved.getStatus().name(),
                "Cliente #" + saved.getClientId(),
                "Solicitud de reserva radicada en la plataforma"
        ));

        return RentalResponse.fromDomain(saved);
    }
}
