package com.rentatools.domain.rental;

import java.time.Instant;

public record RentalTimelineEvent(
        Long id,
        Long rentalId,
        String previousStatus,
        String newStatus,
        String changedBy,
        String reason,
        Instant createdAt
) {
    public static RentalTimelineEvent create(Long rentalId, String previousStatus, String newStatus, String changedBy, String reason) {
        return new RentalTimelineEvent(null, rentalId, previousStatus, newStatus, changedBy, reason, Instant.now());
    }
}
