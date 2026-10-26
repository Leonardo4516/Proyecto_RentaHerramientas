package com.rentatools.domain.rental;

import com.rentatools.domain.common.BusinessRuleValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    @Test
    @DisplayName("Debe calcular correctamente la cantidad de días entre dos fechas")
    void shouldCalculateTotalDays() {
        DateRange range = new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 6));
        assertThat(range.totalDays()).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe lanzar excepción si fecha fin es anterior a fecha inicio")
    void shouldThrowWhenEndBeforeStart() {
        assertThatThrownBy(() -> new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 5)))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("anterior");
    }

    @Test
    @DisplayName("Debe detectar solapamiento de fechas correctamente")
    void shouldDetectDateOverlap() {
        DateRange rangeA = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 10));

        // Rango B intersecta en el medio
        DateRange rangeB = new DateRange(LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 15));
        assertThat(rangeA.overlapsWith(rangeB)).isTrue();

        // Rango C completamente contenido
        DateRange rangeC = new DateRange(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 8));
        assertThat(rangeA.overlapsWith(rangeC)).isTrue();

        // Rango D posterior sin intersección
        DateRange rangeD = new DateRange(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 15));
        assertThat(rangeA.overlapsWith(rangeD)).isFalse();

        // Rango E anterior sin intersección
        DateRange rangeE = new DateRange(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 10, 4));
        assertThat(rangeA.overlapsWith(rangeE)).isFalse();
    }
}
