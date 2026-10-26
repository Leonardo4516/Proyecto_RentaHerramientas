package com.rentatools.application.rental.usecase;

import com.rentatools.application.rental.dto.DamageItemRequest;
import com.rentatools.application.rental.dto.DamageReportDto;
import com.rentatools.application.rental.dto.RegisterReturnInspectionCommand;
import com.rentatools.application.rental.dto.ReturnInspectionResponse;
import com.rentatools.domain.billing.Payment;
import com.rentatools.domain.billing.PaymentMethod;
import com.rentatools.domain.billing.PaymentStatus;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.common.EntityNotFoundException;
import com.rentatools.domain.notification.port.NotificationPort;
import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DamageReport;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.domain.rental.RentalTimelineEvent;
import com.rentatools.domain.rental.ReturnInspection;
import com.rentatools.domain.tool.ToolRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegisterReturnInspectionUseCase {

    private final RentalRepository rentalRepository;
    private final ToolRepository toolRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationPort notificationPort;

    public RegisterReturnInspectionUseCase(RentalRepository rentalRepository,
                                           ToolRepository toolRepository,
                                           PaymentRepository paymentRepository,
                                           NotificationPort notificationPort) {
        this.rentalRepository = rentalRepository;
        this.toolRepository = toolRepository;
        this.paymentRepository = paymentRepository;
        this.notificationPort = notificationPort;
    }

    public RegisterReturnInspectionUseCase(RentalRepository rentalRepository,
                                           ToolRepository toolRepository) {
        this(rentalRepository, toolRepository, null, null);
    }

    public ReturnInspectionResponse execute(Long rentalId, RegisterReturnInspectionCommand command, String executedBy) {
        RentalBooking rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Alquiler no encontrado con ID: " + rentalId));

        BookingStatus previousStatus = rental.getStatus();

        // Calcular penalizaciones por daños
        BigDecimal totalPenalties = BigDecimal.ZERO;
        List<DamageReport> domainDamages = new ArrayList<>();

        if (command.damages() != null && !command.damages().isEmpty()) {
            for (DamageItemRequest item : command.damages()) {
                BigDecimal fee = item.penaltyFee() != null ? item.penaltyFee() : BigDecimal.ZERO;
                totalPenalties = totalPenalties.add(fee);
                domainDamages.add(new DamageReport(null, item.description(), fee, item.evidenceUrl(), Instant.now()));
            }
        }

        BigDecimal originalDeposit = rental.getDepositAmount() != null ? rental.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal depositRefunded = originalDeposit.subtract(totalPenalties);
        if (depositRefunded.compareTo(BigDecimal.ZERO) < 0) {
            depositRefunded = BigDecimal.ZERO;
        }

        boolean hasDamage = totalPenalties.compareTo(BigDecimal.ZERO) > 0 || !domainDamages.isEmpty();

        ReturnInspection inspection = new ReturnInspection(
                null,
                Instant.now(),
                command.inspectorUserId(),
                command.equipmentCondition(),
                hasDamage,
                depositRefunded,
                command.notes(),
                domainDamages
        );

        rental.completeReturn(inspection);
        rentalRepository.save(rental);

        ReturnInspection savedInspection = rentalRepository.saveInspection(rental.getId(), inspection);

        // Actualizar estado de la herramienta
        toolRepository.findById(rental.getToolId()).ifPresent(tool -> {
            String cond = command.equipmentCondition() != null ? command.equipmentCondition().toUpperCase() : "";
            if (cond.contains("GRAVE") || cond.contains("MANTENIMIENTO")) {
                tool.sendToMaintenance();
            } else {
                tool.markAsAvailable();
            }
            toolRepository.save(tool);
        });

        // Registrar evento en la auditoría de la línea de tiempo
        rentalRepository.recordTimelineEvent(new RentalTimelineEvent(
                null,
                rental.getId(),
                previousStatus.name(),
                BookingStatus.COMPLETADA.name(),
                executedBy != null && !executedBy.isBlank() ? executedBy : "Inspector Técnico",
                "Devolución física completada. Estado de equipo: " + command.equipmentCondition() +
                        ". Depósito a reembolsar: $" + depositRefunded,
                Instant.now()
        ));

        // Reembolso automático si está habilitado y hay saldo
        if (Boolean.TRUE.equals(command.autoRefundDeposit()) && depositRefunded.compareTo(BigDecimal.ZERO) > 0 && paymentRepository != null) {
            try {
                Payment refundPayment = new Payment();
                refundPayment.setRentalId(rental.getId());
                refundPayment.setClientId(rental.getClientId());
                refundPayment.setAmount(depositRefunded);
                refundPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                refundPayment.setStatus(PaymentStatus.REFUNDED);
                refundPayment.setTransactionReference("REFUND-" + rental.getId() + "-" + System.currentTimeMillis());
                refundPayment.setPaymentDate(LocalDateTime.now());
                refundPayment.setNotes("Reembolso de depósito tras inspección #" + (savedInspection.getId() != null ? savedInspection.getId() : rental.getId()));
                paymentRepository.save(refundPayment);
            } catch (Exception ignored) {
                // Registrar log sin interrumpir retorno
            }
        }

        if (notificationPort != null) {
            notificationPort.sendToClient(rental.getClientId(), "/topic/client/" + rental.getClientId(),
                    "Tu alquiler #" + rental.getId() + " ha sido recibido e inspeccionado. Depósito reembolsado: $" + depositRefunded);
            notificationPort.broadcast("/topic/catalog", "Maquinaria liberada tras devolución del alquiler #" + rental.getId());
        }

        List<DamageReportDto> damageDtos = savedInspection.getDamages().stream()
                .map(d -> new DamageReportDto(d.getId(), d.getDescription(), d.getPenaltyFee(), d.getEvidenceUrl(), d.getReportedAt()))
                .toList();

        return new ReturnInspectionResponse(
                savedInspection.getId(),
                rental.getId(),
                savedInspection.getReturnDate(),
                savedInspection.getInspectorUserId(),
                savedInspection.getEquipmentCondition(),
                savedInspection.isHasDamage(),
                originalDeposit,
                totalPenalties,
                savedInspection.getDepositRefunded(),
                savedInspection.getNotes(),
                damageDtos
        );
    }
}
