package com.marquify.beta.controllers;

import com.marquify.beta.request.PersonalizacaoCatalogoRequest;
import com.marquify.beta.response.EstabelecimentoPublicoResponse;
import com.marquify.beta.service.PersonalizacaoCatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendedor/me/personalizacao")
@RequiredArgsConstructor
public class PersonalizacaoCatalogoController {
    private final PersonalizacaoCatalogoService service;

    @GetMapping
    public ResponseEntity<EstabelecimentoPublicoResponse> obter() { return ResponseEntity.ok(service.obter()); }

    @PutMapping
    public ResponseEntity<EstabelecimentoPublicoResponse> salvar(@Valid @RequestBody PersonalizacaoCatalogoRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }
}
