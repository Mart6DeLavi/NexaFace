package com.nextface.face;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * In-memory failed-attempt counter for face login. A successful login clears the
 * counter; the window slides from the first failure. Single-instance only.
 */
@Component
public class LoginAttemptService {

    private final int maxAttempts;
    private final Duration window;
    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${nextface.face.login.max-attempts}") int maxAttempts,
            @Value("${nextface.face.login.attempt-window}") Duration window) {
        this(maxAttempts, window, Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration window, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.window = window;
        this.clock = clock;
    }

    /**
     * Throws when the key already used all allowed attempts inside the window.
     */
    public void assertAllowed(String key) {
        AttemptWindow attempt = attempts.get(key);
        if (attempt == null) {
            return;
        }
        if (isExpired(attempt)) {
            attempts.remove(key, attempt);
            return;
        }
        if (attempt.failures() >= maxAttempts) {
            throw new TooManyLoginAttemptsException();
        }
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        attempts.compute(key, (ignored, attempt) -> {
            if (attempt == null || isExpired(attempt)) {
                return new AttemptWindow(now, 1);
            }
            return new AttemptWindow(attempt.windowStart(), attempt.failures() + 1);
        });
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private boolean isExpired(AttemptWindow attempt) {
        return clock.instant().isAfter(attempt.windowStart().plus(window));
    }

    private record AttemptWindow(Instant windowStart, int failures) {
    }
}
