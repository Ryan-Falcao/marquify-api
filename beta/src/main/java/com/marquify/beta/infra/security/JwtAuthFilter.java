package com.marquify.beta.infra.security;

import com.marquify.beta.service.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final AuthorizationService authorizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            String subject = header.startsWith("Bearer ") ? tokenService.validarToken(header.substring(7)) : null;
            if (subject == null) {
                SecurityContextHolder.clearContext();
                SecurityErrors.unauthorized(response);
                return;
            }
            try {
                var usuario = authorizationService.loadTokenSubject(subject);
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException exception) {
                SecurityContextHolder.clearContext();
                SecurityErrors.unauthorized(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
