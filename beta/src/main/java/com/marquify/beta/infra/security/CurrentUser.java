package com.marquify.beta.infra.security;

import com.marquify.beta.entity.Cliente;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.entity.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public Object principal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Cliente || authentication.getPrincipal() instanceof Vendedor)) {
            throw new AuthenticationCredentialsNotFoundException("Autenticação necessária");
        }
        return authentication.getPrincipal();
    }

    public Cliente cliente() {
        if (principal() instanceof Cliente cliente) return cliente;
        throw new AccessDeniedException("Operação exclusiva do cliente");
    }

    public Vendedor vendedor(Long requestedId) {
        if (!(principal() instanceof Vendedor vendedor) || vendedor.getRole() != UserRole.ADMIN
                || requestedId == null || !requestedId.equals(vendedor.getId())) {
            throw new AccessDeniedException("Acesso negado");
        }
        return vendedor;
    }

    public Vendedor vendedor() {
        if (!(principal() instanceof Vendedor vendedor) || vendedor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }
        return vendedor;
    }
}
