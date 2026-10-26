package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.DamageReportDto;
import com.rentatools.application.rental.dto.ReturnInspectionResponse;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.ReturnInspection;

import java.math.BigDecimal;
import java.util.List;

public class GetReturnInspectionUseCase {

    private final RentalRepository rentalRepository;

    public GetReturnInspectionUseCase(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public ReturnInspectionResponse execute(Long rentalId) {
        RentalBooking rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId));

        ReturnInspection inspection = rentalRepository.findInspectionByRentalId(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("No se ha registrado informe de inspección técnica para el alquiler #" + rentalId));

        BigDecimal originalDeposit = rental.getDepositAmount() != null ? rental.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal totalPenalties = inspection.getDamages().stream()
                .map(d -> d.getPenaltyFee() != null ? d.getPenaltyFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DamageReportDto> damageDtos = inspection.getDamages().stream()
                .map(d -> new DamageReportDto(d.getId(), d.getDescription(), d.getPenaltyFee(), d.getEvidenceUrl(), d.getReportedAt()))
                .toList();

        return new ReturnInspectionResponse(
                inspection.getId(),
                rental.getId(),
                inspection.getReturnDate(),
                inspection.getInspectorUserId(),
                inspection.getEquipmentCondition(),
                inspection.isHasDamage(),
                originalDeposit,
                totalPenalties,
                inspection.getDepositRefunded(),
                inspection.getNotes(),
                damageDtos
        );
    }
}
