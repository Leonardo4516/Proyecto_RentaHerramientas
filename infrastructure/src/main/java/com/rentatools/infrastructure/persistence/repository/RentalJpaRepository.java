package com.rentatools.infrastructure.persistence.repository;

import com.rentatools.infrastructure.persistence.entity.RentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface RentalJpaRepository extends JpaRepository<RentalEntity, Long> {
    List<RentalEntity> findByClientId(Long clientId);
    List<RentalEntity> findByToolId(Long toolId);

    @Query("SELECT r FROM RentalEntity r WHERE r.toolId = :toolId AND r.status IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')")
    List<RentalEntity> findActiveRentalsByToolId(@Param("toolId") Long toolId);

    /**
     * Comprueba si existe solapamiento:
     * Una reserva existente [r.startDate, r.endDate] se solapa con [startDate, endDate] si:
     * r.startDate <= :endDate AND r.endDate >= :startDate
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM RentalEntity r
        WHERE r.toolId = :toolId
          AND r.status IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
          AND r.startDate <= :endDate
          AND r.endDate >= :startDate
    """)
    boolean existsOverlappingBooking(
            @Param("toolId") Long toolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(r) > 0 FROM RentalEntity r
        WHERE r.toolId = :toolId
          AND r.id != :rentalId
          AND r.status IN ('APROBADA', 'CONFIRMADA', 'EN_CURSO')
          AND r.startDate <= :endDate
          AND r.endDate >= :startDate
    """)
    boolean existsOtherOverlappingConfirmedBooking(
            @Param("toolId") Long toolId,
            @Param("rentalId") Long rentalId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
