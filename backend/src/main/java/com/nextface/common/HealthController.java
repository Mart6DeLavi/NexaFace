package com.nextface.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness endpoint used to verify that the backend is up.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", "nextface-backend");
    }
}
