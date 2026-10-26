package com.rentatools.domain.user;

public enum Role {
    ROLE_ADMIN,
    ROLE_PROVEEDOR,
    ROLE_CLIENTE;

    public static Role fromString(String roleName) {
        if (roleName == null) return null;
        String normalized = roleName.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        for (Role r : values()) {
            if (r.name().equals(normalized)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol inválido: " + roleName);
    }
}
