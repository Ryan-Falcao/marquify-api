package com.marquify.beta.service;

import com.marquify.beta.entity.Cliente;
import com.marquify.beta.infra.security.TokenService;
import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.CadastroClienteRapidoRequest;
import com.marquify.beta.response.CadastroClienteRapidoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CadastroClienteService {
    private final clienteRepository clientes;
    private final vendedorRepository vendedores;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public CadastroClienteRapidoResponse cadastrar(CadastroClienteRapidoRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (clientes.existsByEmailIgnoreCase(email) || vendedores.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este e-mail já possui cadastro. Entre na sua conta para continuar.");
        }
        Cliente cliente = new Cliente(email, passwordEncoder.encode(request.senha()));
        cliente.setNome(request.nome().trim());
        cliente.setNumero(request.numero().trim());
        cliente = clientes.save(cliente);
        return new CadastroClienteRapidoResponse(tokens.gerarToken(cliente), cliente.getId(), cliente.getNome());
    }
}
