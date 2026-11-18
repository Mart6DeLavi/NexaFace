package com.nextface.face;

import java.time.Instant;
import java.util.UUID;

/**
 * Public representation of an enrolled face profile.
 */
public record FaceProfileResponse(UUID id, Instant enrolledAt, Instant updatedAt) {

    static FaceProfileResponse from(FaceProfile profile) {
        return new FaceProfileResponse(profile.getId(), profile.getCreatedAt(), profile.getUpdatedAt());
    }
}
