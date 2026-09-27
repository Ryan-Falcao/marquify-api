package com.marquify.beta.infra.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Bounded, thread-safe, per-instance fixed window. All login attempts count. */
@Component
public class LoginAttemptLimiter {
    private record Window(long expires, int attempts) {}
    private final Map<String, Window> windows = new HashMap<>();
    private final int limit, capacity;
    private final long windowNanos;
    private final LongSupplier time;
    private long nextCleanup;

    @Autowired
    public LoginAttemptLimiter(@Value("${api.login-rate-limit.max-attempts:10}") int limit,
                               @Value("${api.login-rate-limit.window-seconds:60}") long seconds,
                               @Value("${api.login-rate-limit.max-clients:10000}") int capacity) {
        this(limit, seconds, capacity, System::nanoTime);
    }
    LoginAttemptLimiter(int limit, long seconds, int capacity, LongSupplier time) {
        if (limit < 1 || seconds < 1 || seconds > 86400 || capacity < 1) throw new IllegalArgumentException("Invalid login rate limit");
        this.limit = limit; this.capacity = capacity; this.windowNanos = seconds * 1_000_000_000L; this.time = time;
    }
    /** Returns 0 if accepted, otherwise Retry-After seconds. */
    public synchronized long acquire(String address) {
        long now = time.getAsLong();
        if (now >= nextCleanup) {
            windows.values().removeIf(w -> w.expires() <= now);
            nextCleanup = now + 1_000_000_000L;
        }
        Window w = windows.get(address);
        if (w == null || w.expires() <= now) {
            if (w == null && windows.size() >= capacity) return 1;
            windows.put(address, new Window(now + windowNanos, 1)); return 0;
        }
        if (w.attempts() >= limit) return Math.max(1, (w.expires() - now + 999_999_999L) / 1_000_000_000L);
        windows.put(address, new Window(w.expires(), w.attempts() + 1)); return 0;
    }
}
