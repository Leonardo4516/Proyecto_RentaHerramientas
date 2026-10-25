package com.rentatools.infrastructure.persistence.adapter;

import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.infrastructure.persistence.entity.RentalEntity;
import com.rentatools.infrastructure.persistence.repository.RentalJpaRepository;
import com.rentatools.infrastructure.persistence.user.ClientJpaEntity;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import com.rentatools.domain.rental.RentalTimelineEvent;
import com.rentatools.infrastructure.persistence.entity.RentalAuditLogEntity;
import com.rentatools.infrastructure.persistence.repository.RentalAuditLogJpaRepository;
import com.rentatools.domain.rental.ReturnInspection;
import com.rentatools.domain.rental.DamageReport;
import com.rentatools.infrastructure.persistence.entity.RentalReturnEntity;
import com.rentatools.infrastructure.persistence.entity.ReturnDamageEntity;
import com.rentatools.infrastructure.persistence.repository.RentalReturnJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Component
public class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository rentalJpaRepository;
    private final SpringDataClientRepository clientRepository;
    private final RentalAuditLogJpaRepository auditLogRepository;
    private final RentalReturnJpaRepository rentalReturnJpaRepository;

    public RentalRepositoryAdapter(RentalJpaRepository rentalJpaRepository,
                                  SpringDataClientRepository clientRepository,
                                  RentalAuditLogJpaRepository auditLogRepository,
                                  RentalReturnJpaRepository rentalReturnJpaRepository) {
        this.rentalJpaRepository = rentalJpaRepository;
        this.clientRepository = clientRepository;
        this.auditLogRepository = auditLogRepository;
        this.rentalReturnJpaRepository = rentalReturnJpaRepository;
    }

    private Long resolveClientId(Long id) {
        if (id == null) return 1L;
        if (clientRepository.existsById(id)) {
            return id;
        }
        return clientRepository.findByUserId(id)
                .map(ClientJpaEntity::getId)
                .orElse(id);
    }

    @Override
    public RentalBooking save(RentalBooking domain) {
        RentalEntity entity = new RentalEntity();
        entity.setId(domain.getId());
        entity.setClientId(resolveClientId(domain.getClientId()));
        entity.setToolId(domain.getToolId());
        entity.setStartDate(domain.getDateRange().startDate());
        entity.setEndDate(domain.getDateRange().endDate());
        entity.setTotalDays(domain.getDateRange().totalDays());
        entity.setDailyRate(domain.getDailyRate());
        entity.setDepositAmount(domain.getDepositAmount());
        entity.setSubtotal(domain.getSubtotal());
        entity.setStatus(domain.getStatus().name());
        entity.setDeliveryAddress(domain.getDeliveryAddress());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());

        RentalEntity saved = rentalJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RentalBooking> findById(Long id) {
        return rentalJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<RentalBooking> findByClientId(Long clientId) {
        Long resolved = resolveClientId(clientId);
        return rentalJpaRepository.findByClientId(resolved).stream().map(this::toDomain).toList();
    }

    @Override
    public List<RentalBooking> findByToolId(Long toolId) {
        return rentalJpaRepository.findByToolId(toolId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<RentalBooking> findActiveRentalsByToolId(Long toolId) {
        return rentalJpaRepository.findActiveRentalsByToolId(toolId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<RentalBooking> findAll() {
        return rentalJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean hasOverlappingBookings(Long toolId, LocalDate startDate, LocalDate endDate) {
        return rentalJpaRepository.existsOverlappingBooking(toolId, startDate, endDate);
    }

    @Override
    public boolean hasOtherOverlappingConfirmedBooking(Long toolId, Long rentalId, LocalDate startDate, LocalDate endDate) {
        return rentalJpaRepository.existsOtherOverlappingConfirmedBooking(toolId, rentalId, startDate, endDate);
    }

    @Override
    public void recordTimelineEvent(RentalTimelineEvent event) {
        if (event == null || event.rentalId() == null) return;
        try {
            RentalAuditLogEntity entity = new RentalAuditLogEntity(
                    event.rentalId(),
                    event.previousStatus(),
                    event.newStatus(),
                    event.changedBy() != null ? event.changedBy() : "Sistema",
                    event.reason()
            );
            if (event.createdAt() != null) {
                entity.setCreatedAt(event.createdAt());
            }
            auditLogRepository.save(entity);
        } catch (Exception ignored) {
            // No interrumpir la transacción principal en caso de error de logging
        }
    }

    @Override
    public List<RentalTimelineEvent> findTimelineByRentalId(Long rentalId) {
        return auditLogRepository.findByRentalIdOrderByCreatedAtAsc(rentalId).stream()
                .map(e -> new RentalTimelineEvent(
                        e.getId(),
                        e.getRentalId(),
                        e.getPreviousStatus(),
                        e.getNewStatus(),
                        e.getChangedBy(),
                        e.getReason(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public ReturnInspection saveInspection(Long rentalId, ReturnInspection inspection) {
        if (rentalId == null || inspection == null) return inspection;
        RentalReturnEntity entity = rentalReturnJpaRepository.findByRentalId(rentalId)
                .orElseGet(() -> new RentalReturnEntity(
                        rentalId,
                        inspection.getReturnDate(),
                        inspection.getInspectorUserId(),
                        inspection.getEquipmentCondition(),
                        inspection.isHasDamage(),
                        inspection.getDepositRefunded(),
                        inspection.getNotes()
                ));

        entity.setReturnDate(inspection.getReturnDate());
        entity.setInspectorUserId(inspection.getInspectorUserId());
        entity.setEquipmentCondition(inspection.getEquipmentCondition());
        entity.setHasDamage(inspection.isHasDamage());
        entity.setDepositRefunded(inspection.getDepositRefunded());
        entity.setNotes(inspection.getNotes());

        if (inspection.getDamages() != null) {
            List<ReturnDamageEntity> damageEntities = new java.util.ArrayList<>();
            for (DamageReport d : inspection.getDamages()) {
                ReturnDamageEntity de = new ReturnDamageEntity(
                        d.getDescription(),
                        d.getPenaltyFee(),
                        d.getEvidenceUrl(),
                        d.getReportedAt()
                );
                damageEntities.add(de);
            }
            entity.setDamages(damageEntities);
        }

        RentalReturnEntity saved = rentalReturnJpaRepository.save(entity);
        return toInspectionDomain(saved);
    }

    @Override
    public Optional<ReturnInspection> findInspectionByRentalId(Long rentalId) {
        return rentalReturnJpaRepository.findByRentalId(rentalId).map(this::toInspectionDomain);
    }

    private ReturnInspection toInspectionDomain(RentalReturnEntity e) {
        List<DamageReport> damages = new java.util.ArrayList<>();
        if (e.getDamages() != null) {
            for (ReturnDamageEntity d : e.getDamages()) {
                damages.add(new DamageReport(
                        d.getId(),
                        d.getDescription(),
                        d.getPenaltyFee(),
                        d.getEvidenceUrl(),
                        d.getReportedAt()
                ));
            }
        }
        return new ReturnInspection(
                e.getId(),
                e.getReturnDate(),
                e.getInspectorUserId(),
                e.getEquipmentCondition(),
                Boolean.TRUE.equals(e.getHasDamage()),
                e.getDepositRefunded(),
                e.getNotes(),
                damages
        );
    }

    private RentalBooking toDomain(RentalEntity e) {
        DateRange dateRange = new DateRange(e.getStartDate(), e.getEndDate());
        BookingStatus status = BookingStatus.valueOf(e.getStatus());
        return new RentalBooking(
                e.getId(),
                e.getClientId(),
                e.getToolId(),
                dateRange,
                e.getDailyRate(),
                e.getDepositAmount(),
                e.getSubtotal(),
                status,
                e.getDeliveryAddress(),
                e.getNotes(),
                e.getCreatedAt()
        );
    }
}
