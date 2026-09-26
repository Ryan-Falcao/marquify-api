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
import com.marquify.beta.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private CadastroComercialService cadastroComercialService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private vendedorRepository vendedorRepository;
    @Autowired
    private clienteRepository clienteRepository;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationRequest request){
        var usernameSenha = new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha());
        var auth = authenticationManager.authenticate(usernameSenha);

        String token = tokenService.gerarToken((UserDetails) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterRequest request){
        if (this.clienteRepository.existsByEmail(request.getLogin()) || this.vendedorRepository.existsByEmail(request.getLogin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login indisponível");
        }

        String encryptedSenha = new BCryptPasswordEncoder().encode(request.getSenha());
        Cliente newUser = new Cliente(request.getLogin(), encryptedSenha);

        this.clienteRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/cadastro-comercial")
    public ResponseEntity<CadastroComercialResponse> cadastroComercial(@RequestBody @Valid CadastroComercialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroComercialService.cadastrar(request));
    }
}
