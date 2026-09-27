package com.marquify.beta.infra;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String id = UUID.randomUUID().toString();
        long start = System.nanoTime(); boolean failed = false;
        response.setHeader("X-Request-ID", id);
        try (var ignored = MDC.putCloseable("requestId", id)) {
            try { chain.doFilter(request,response); }
            catch (IOException | ServletException | RuntimeException e) { failed = true; throw e; }
            finally {
                // No bodies, query strings, credentials, tokens or personal identifiers.
                log.atInfo().addKeyValue("method",request.getMethod())
                        .addKeyValue("route", java.util.Objects.toString(request.getAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE), "unmapped"))
                        .addKeyValue("status", failed ? 500 : response.getStatus())
                        .addKeyValue("durationMs", (System.nanoTime()-start)/1_000_000)
                        .log("http_request_completed");
            }
        }
    }
}
