package com.marquify.beta.infra.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final LoginAttemptLimiter limiter;
    public LoginRateLimitFilter(LoginAttemptLimiter limiter) { this.limiter = limiter; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if ("POST".equals(request.getMethod()) && "/auth/login".equals(request.getServletPath())) {
            long wait = limiter.acquire(request.getRemoteAddr());
            if (wait > 0) {
                response.setStatus(429);
                response.setHeader("Retry-After", Long.toString(wait));
                response.setContentType("application/json"); response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":429,\"codigo\":\"LIMITE_LOGIN\",\"mensagem\":\"Muitas tentativas. Aguarde " + wait + " segundos e tente novamente.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
