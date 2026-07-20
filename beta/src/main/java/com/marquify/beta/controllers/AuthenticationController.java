package com.marquify.beta.controllers;

import com.marquify.beta.entity.Cliente;
import com.marquify.beta.infra.security.TokenService;
import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.AuthenticationRequest;
import com.marquify.beta.request.RegisterRequest;
import com.marquify.beta.response.LoginResponse;
import jakarta.validation.Valid;
import org.apache.catalina.User;
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
        if(this.clienteRepository.findByEmail(request.getLogin()) != null) return ResponseEntity.badRequest().build();

        String encryptedSenha = new BCryptPasswordEncoder().encode(request.getSenha());
        Cliente newUser = new Cliente(request.getLogin(), encryptedSenha);

        this.clienteRepository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
