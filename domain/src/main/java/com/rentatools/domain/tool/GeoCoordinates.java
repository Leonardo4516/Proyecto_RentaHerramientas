package com.rentatools.domain.tool;

import com.rentatools.domain.common.BusinessRuleValidationException;
import java.math.BigDecimal;
import java.util.Objects;

public record GeoCoordinates(BigDecimal latitude, BigDecimal longitude) {
    public GeoCoordinates {
        if (latitude != null && (latitude.doubleValue() < -90.0 || latitude.doubleValue() > 90.0)) {
            throw new BusinessRuleValidationException("La latitud debe estar entre -90 y 90 grados.");
        }
        if (longitude != null && (longitude.doubleValue() < -180.0 || longitude.doubleValue() > 180.0)) {
            throw new BusinessRuleValidationException("La longitud debe estar entre -180 y 180 grados.");
        }
    }

    /**
     * Calcula la distancia en kilómetros a otras coordenadas usando la fórmula de Haversine.
     */
    public double distanceToKm(GeoCoordinates other) {
        if (this.latitude == null || this.longitude == null || other == null || other.latitude == null || other.longitude == null) {
            return Double.MAX_VALUE;
        }
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(other.latitude.doubleValue() - this.latitude.doubleValue());
        double dLng = Math.toRadians(other.longitude.doubleValue() - this.longitude.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude.doubleValue()))
                * Math.cos(Math.toRadians(other.latitude.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
