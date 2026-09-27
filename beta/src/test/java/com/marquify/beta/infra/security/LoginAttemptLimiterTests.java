package com.marquify.beta.infra.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptLimiterTests {
    @Test
    void limitsEachAddressAndExpiresWithoutExtendingBlockedWindow() {
        var now = new AtomicLong();
        var limiter = new LoginAttemptLimiter(2,60,2,now::get);
        assertThat(limiter.acquire("a")).isZero(); assertThat(limiter.acquire("a")).isZero();
        assertThat(limiter.acquire("a")).isEqualTo(60);
        assertThat(limiter.acquire("b")).isZero();
        assertThat(limiter.acquire("c")).isPositive();
        now.set(60_000_000_000L);
        assertThat(limiter.acquire("c")).isZero(); assertThat(limiter.acquire("a")).isZero();
    }
    @Test
    void concurrentAttemptsCannotBypassLimit() throws Exception {
        var limiter = new LoginAttemptLimiter(5,60,100,() -> 0L);
        try (var pool = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<Future<Long>>();
            for (int i=0;i<20;i++) futures.add(pool.submit(() -> limiter.acquire("same-ip")));
            int accepted=0;
            for (var future : futures) if (future.get(5,TimeUnit.SECONDS)==0) accepted++;
            assertThat(accepted).isEqualTo(5);
        }
    }
    @Test
    void filterReturnsRetryAfterAndDoesNotTrustForwardedHeaders() throws Exception {
        var filter = new LoginRateLimitFilter(new LoginAttemptLimiter(1,60,10,() -> 0L));
        for (int i=0;i<2;i++) {
            var request = new MockHttpServletRequest("POST","/auth/login"); request.setServletPath("/auth/login");
            request.setRemoteAddr("192.0.2.1"); request.addHeader("X-Forwarded-For","198.51.100."+i);
            var response = new MockHttpServletResponse();
            filter.doFilter(request,response,(req,res) -> ((jakarta.servlet.http.HttpServletResponse)res).setStatus(204));
            assertThat(response.getStatus()).isEqualTo(i==0 ? 204 : 429);
            if (i==1) { assertThat(response.getHeader("Retry-After")).isEqualTo("60"); assertThat(response.getContentAsString()).contains("LIMITE_LOGIN"); }
        }
        var other = new MockHttpServletRequest("GET","/actuator/health");
        var response = new MockHttpServletResponse();
        filter.doFilter(other,response,(req,res) -> ((jakarta.servlet.http.HttpServletResponse)res).setStatus(204));
        assertThat(response.getStatus()).isEqualTo(204);
    }
}
