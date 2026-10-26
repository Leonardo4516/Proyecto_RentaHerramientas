package com.rentatools.domain.tool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GeoCoordinatesTest {

    @Test
    @DisplayName("Debe calcular distancia Haversine aproximada entre Bogotá y Chía (~20 km)")
    void shouldCalculateDistanceBetweenLocations() {
        GeoCoordinates bogota = new GeoCoordinates(new BigDecimal("4.6534"), new BigDecimal("-74.0836"));
        GeoCoordinates chia = new GeoCoordinates(new BigDecimal("4.8622"), new BigDecimal("-74.0583"));

        double distance = bogota.distanceToKm(chia);

        assertThat(distance).isBetween(20.0, 26.0);
    }
}
