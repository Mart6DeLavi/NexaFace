package com.nextface.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-bytes-long";

    private final JwtService jwtService = new JwtService(SECRET, Duration.ofMinutes(15));

    @Test
    void generatesTokenThatCanBeParsed() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "user@example.com");
        AuthenticatedUser parsed = jwtService.parseToken(token);

        assertThat(parsed.id()).isEqualTo(userId);
        assertThat(parsed.email()).isEqualTo("user@example.com");
        assertThat(jwtService.expirationSeconds()).isEqualTo(900);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtService other = new JwtService("another-secret-key-with-at-least-32-bytes!", Duration.ofMinutes(15));
        String token = other.generateToken(UUID.randomUUID(), "user@example.com");

        assertThatThrownBy(() -> jwtService.parseToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService expired = new JwtService(SECRET, Duration.ofSeconds(-1));
        String token = expired.generateToken(UUID.randomUUID(), "user@example.com");

        assertThatThrownBy(() -> jwtService.parseToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsSecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService("too-short", Duration.ofMinutes(5)))
                .isInstanceOf(IllegalStateException.class);
    }
}
