package com.rentatools.infrastructure.persistence.adapter;

import com.rentatools.domain.rental.BookingStatus;
import com.rentatools.domain.rental.DateRange;
import com.rentatools.domain.rental.RentalBooking;
import com.rentatools.domain.rental.RentalRepository;
import com.rentatools.infrastructure.persistence.entity.RentalEntity;
import com.rentatools.infrastructure.persistence.repository.RentalJpaRepository;
import com.rentatools.infrastructure.persistence.user.ClientJpaEntity;
import com.rentatools.infrastructure.persistence.user.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Component
public class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository rentalJpaRepository;
    private final SpringDataClientRepository clientRepository;

    public RentalRepositoryAdapter(RentalJpaRepository rentalJpaRepository, SpringDataClientRepository clientRepository) {
        this.rentalJpaRepository = rentalJpaRepository;
        this.clientRepository = clientRepository;
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
