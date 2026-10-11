package com.rentatools.domain.rental;

import com.rentatools.domain.common.BusinessRuleValidationException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record DateRange(LocalDate startDate, LocalDate endDate) {
    public DateRange {
        Objects.requireNonNull(startDate, "La fecha de inicio no puede ser nula.");
        Objects.requireNonNull(endDate, "La fecha de fin no puede ser nula.");
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleValidationException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    public int totalDays() {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return (int) Math.max(1, days);
    }

    /**
     * Regla matemática de solapamiento:
     * Dos rangos [A_start, A_end] y [B_start, B_end] se solapan si y solo si:
     * A_start <= B_end AND A_end >= B_start
     */
    public boolean overlapsWith(DateRange other) {
        if (other == null) return false;
        return !this.startDate.isAfter(other.endDate) && !this.endDate.isBefore(other.startDate);
    }
}
