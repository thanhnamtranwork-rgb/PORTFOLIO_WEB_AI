package com.motelona.assistant.service;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** Per-instance request throttle. Use a shared gateway/Redis limiter when running multiple instances. */
@Service
public class RateLimitService {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    public void check(String key) {
        check("general", key, 50, Duration.ofMinutes(1));
    }

    public void check(String bucket, String key, int limit, Duration duration) {
        if (limit < 1 || duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Rate-limit configuration is invalid.");
        }
        String safeKey = (bucket == null ? "default" : bucket) + ":" +
                (key == null || key.isBlank() ? "unknown" : key.trim());
        long now = clock.millis();
        long windowMs = Math.max(1_000L, duration.toMillis());
        Window window = windows.compute(safeKey, (ignored, current) -> {
            if (current == null || now - current.startedAt() >= windowMs) return new Window(now, 1, windowMs);
            return new Window(current.startedAt(), current.count() + 1, windowMs);
        });
        if (window.count() > limit) {
            throw new RateLimitException("Too many requests. Please wait before trying again.");
        }
        if (windows.size() > 20_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() > entry.getValue().windowMs() * 2);
        }
    }

    private record Window(long startedAt, int count, long windowMs) { }
}
