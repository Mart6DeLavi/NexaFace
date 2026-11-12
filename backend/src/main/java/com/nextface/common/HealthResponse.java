package com.nextface.common;

/**
 * Health check response payload.
 */
public record HealthResponse(String status, String service) {
}
