package com.nextface.face;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-18T10:00:00Z"));
    private final LoginAttemptService service =
            new LoginAttemptService(3, Duration.ofMinutes(15), clock);

    @Test
    void allowsAttemptsBelowLimit() {
        service.recordFailure("user@example.com");
        service.recordFailure("user@example.com");

        assertThatCode(() -> service.assertAllowed("user@example.com")).doesNotThrowAnyException();
    }

    @Test
    void blocksAfterLimitIsReached() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("user@example.com");
        }

        assertThatThrownBy(() -> service.assertAllowed("user@example.com"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void allowsAgainAfterWindowExpires() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("user@example.com");
        }

        clock.advance(Duration.ofMinutes(16));

        assertThatCode(() -> service.assertAllowed("user@example.com")).doesNotThrowAnyException();
    }

    @Test
    void successfulLoginResetsCounter() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("user@example.com");
        }

        service.reset("user@example.com");

        assertThatCode(() -> service.assertAllowed("user@example.com")).doesNotThrowAnyException();
    }

    @Test
    void separateKeysHaveSeparateCounters() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("first@example.com");
        }

        assertThatCode(() -> service.assertAllowed("second@example.com")).doesNotThrowAnyException();
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
