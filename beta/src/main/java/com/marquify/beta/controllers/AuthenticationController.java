package com.marquify.beta.controllers;

import com.marquify.beta.entity.Cliente;
import com.marquify.beta.infra.security.TokenService;
import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.AuthenticationRequest;
import com.marquify.beta.request.RegisterRequest;
import com.marquify.beta.request.CadastroComercialRequest;
import com.marquify.beta.response.CadastroComercialResponse;
import com.marquify.beta.service.CadastroComercialService;
import com.marquify.beta.service.CadastroClienteService;
import com.marquify.beta.request.CadastroClienteRapidoRequest;
import com.marquify.beta.response.CadastroClienteRapidoResponse;
import com.marquify.beta.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final CadastroComercialService cadastroComercialService;
    private final CadastroClienteService cadastroClienteService;
    private final AuthenticationManager authenticationManager;
    private final vendedorRepository vendedorRepository;
    private final clienteRepository clienteRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(
            CadastroComercialService cadastroComercialService,
            CadastroClienteService cadastroClienteService,
            AuthenticationManager authenticationManager,
            vendedorRepository vendedorRepository,
            clienteRepository clienteRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder) {
        this.cadastroComercialService = cadastroComercialService;
        this.cadastroClienteService = cadastroClienteService;
        this.authenticationManager = authenticationManager;
        this.vendedorRepository = vendedorRepository;
        this.clienteRepository = clienteRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        var usernameSenha = new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha());
        var auth = authenticationManager.authenticate(usernameSenha);

        String token = tokenService.gerarToken((UserDetails) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        String email = request.getLogin().trim().toLowerCase(Locale.ROOT);
        if (this.clienteRepository.existsByEmailIgnoreCase(email) || this.vendedorRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login indisponível");
        }

        String encryptedSenha = passwordEncoder.encode(request.getSenha());
        Cliente newUser = new Cliente(email, encryptedSenha);

        this.clienteRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/cadastro-comercial")
    public ResponseEntity<CadastroComercialResponse> cadastroComercial(@RequestBody @Valid CadastroComercialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroComercialService.cadastrar(request));
    }

    @PostMapping("/cadastro-cliente")
    public ResponseEntity<CadastroClienteRapidoResponse> cadastroCliente(@RequestBody @Valid CadastroClienteRapidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroClienteService.cadastrar(request));
    }
}
