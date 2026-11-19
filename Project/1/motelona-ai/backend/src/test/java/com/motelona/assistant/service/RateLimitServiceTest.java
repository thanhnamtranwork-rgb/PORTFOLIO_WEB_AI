package com.motelona.assistant.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import java.time.Duration;

class RateLimitServiceTest {
    @Test
    void enforcesSharedFreeProviderCallBucket() {
        RateLimitService service = new RateLimitService();
        for (int i = 0; i < 15; i++) {
            service.check("local-ai-minute", "all-visitors", 15, Duration.ofMinutes(1));
        }
        assertThrows(RateLimitException.class, () ->
                service.check("local-ai-minute", "all-visitors", 15, Duration.ofMinutes(1)));
    }

    @Test
    void appliesIndependentBucketLimits() {
        RateLimitService service = new RateLimitService();
        service.check("chat", "127.0.0.1", 1, Duration.ofMinutes(1));
        assertThrows(RateLimitException.class, () -> service.check("chat", "127.0.0.1", 1, Duration.ofMinutes(1)));
        service.check("memory", "127.0.0.1", 1, Duration.ofMinutes(1));
    }
}
