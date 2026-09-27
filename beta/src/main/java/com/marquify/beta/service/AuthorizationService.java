package com.marquify.beta.service;

import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthorizationService implements UserDetailsService {
    private final vendedorRepository vendedores;
    private final clienteRepository clientes;

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (username == null) throw unknown();
        String email = username.trim().toLowerCase(java.util.Locale.ROOT);
        var matchingVendedores = vendedores.findAllByEmailIgnoreCase(email);
        var matchingClientes = clientes.findAllByEmailIgnoreCase(email);
        // Não escolher uma identidade arbitrária se houver dados legados duplicados.
        if (matchingVendedores.size() + matchingClientes.size() != 1) throw unknown();
        return matchingVendedores.isEmpty() ? matchingClientes.getFirst() : matchingVendedores.getFirst();
    }

    public UserDetails loadTokenSubject(String subject) {
        if (subject == null) throw unknown();
        String[] parts = subject.split(":", -1);
        if (parts.length != 2) throw unknown();
        try {
            long id = Long.parseLong(parts[1]);
            if (id <= 0) throw unknown();
            return switch (parts[0]) {
                case "cliente" -> clientes.findById(id).orElseThrow(this::unknown);
                case "vendedor" -> vendedores.findById(id).orElseThrow(this::unknown);
                default -> throw unknown();
            };
        } catch (NumberFormatException exception) {
            throw unknown();
        }
    }

    private UsernameNotFoundException unknown() {
        return new UsernameNotFoundException("Credenciais inválidas");
    }
}
