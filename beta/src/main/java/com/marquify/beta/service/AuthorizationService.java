package com.marquify.beta.service;

import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service

public class AuthorizationService implements UserDetailsService {

    @Autowired
    vendedorRepository vendedorRepository;

    @Autowired
    clienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails vendedor = vendedorRepository.findByEmail(username);
        if (vendedor != null) {
            return vendedor;
        }

        UserDetails cliente = clienteRepository.findByEmail(username);
        if (cliente != null) {
            return cliente;
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}
