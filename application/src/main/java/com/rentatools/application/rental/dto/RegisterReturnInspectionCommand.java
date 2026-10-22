package com.rentatools.application.rental.dto;

import java.util.List;

public record RegisterReturnInspectionCommand(
        Long inspectorUserId,
        String equipmentCondition,
        String notes,
        List<DamageItemRequest> damages,
        Boolean autoRefundDeposit
) {
    public RegisterReturnInspectionCommand {
        if (damages == null) {
            damages = List.of();
        }
        if (equipmentCondition == null || equipmentCondition.isBlank()) {
            equipmentCondition = "EXCELENTE";
        }
        if (autoRefundDeposit == null) {
            autoRefundDeposit = true;
        }
    }
}
