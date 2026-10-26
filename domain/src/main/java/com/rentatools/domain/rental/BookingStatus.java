package com.rentatools.domain.rental;

public enum BookingStatus {
    PENDIENTE,
    APROBADA,
    CONFIRMADA,
    EN_CURSO,
    COMPLETADA,
    CANCELADA,
    RECHAZADA;

    public static BookingStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return PENDIENTE;
        }
        String upper = value.trim().toUpperCase();
        if ("CONFIRMADA".equals(upper) || "APROBADA".equals(upper)) {
            return APROBADA;
        }
        try {
            return BookingStatus.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return PENDIENTE;
        }
    }
}
