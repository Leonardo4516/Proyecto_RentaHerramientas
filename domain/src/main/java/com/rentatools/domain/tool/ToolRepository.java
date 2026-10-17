package com.rentatools.domain.tool;

import java.util.List;
import java.util.Optional;
public interface ToolRepository {
    Tool save(Tool tool);
    Optional<Tool> findById(Long id);

    default Optional<Tool> findByIdWithLock(Long id) {
        return findById(id);
    }

    Optional<Tool> findByCode(String code);
    List<Tool> findAll();
    List<Tool> findByCategoryId(Long categoryId);
    List<Tool> findByProviderId(Long providerId);
    List<Tool> findByStatus(ToolStatus status);

    default List<Tool> findWithinBoundingBox(java.math.BigDecimal minLat, java.math.BigDecimal maxLat,
                                             java.math.BigDecimal minLng, java.math.BigDecimal maxLng) {
        return findAll().stream()
                .filter(t -> t.getCoordinates() != null
                        && t.getCoordinates().latitude().compareTo(minLat) >= 0
                        && t.getCoordinates().latitude().compareTo(maxLat) <= 0
                        && t.getCoordinates().longitude().compareTo(minLng) >= 0
                        && t.getCoordinates().longitude().compareTo(maxLng) <= 0)
                .toList();
    }
}
