package com.nextface.auth;

import java.util.UUID;

/**
 * Authenticated principal extracted from a valid JWT.
 */
public record AuthenticatedUser(UUID id, String email) {
}
