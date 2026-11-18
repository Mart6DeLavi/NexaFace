package com.nextface.face;

import java.time.Instant;

/**
 * Enrollment status used by the frontend to decide whether to show the scanner.
 */
public record FaceProfileStatusResponse(boolean enrolled, Instant updatedAt) {
}
