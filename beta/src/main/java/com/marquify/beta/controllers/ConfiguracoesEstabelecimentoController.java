package com.marquify.beta.controllers;

import com.marquify.beta.request.ConfiguracoesEstabelecimentoRequest;
import com.marquify.beta.response.ConfiguracoesEstabelecimentoResponse;
import com.marquify.beta.service.ConfiguracoesEstabelecimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendedor/me/configuracoes")
@RequiredArgsConstructor
public class ConfiguracoesEstabelecimentoController {
    private final ConfiguracoesEstabelecimentoService service;

    @GetMapping
    public ResponseEntity<ConfiguracoesEstabelecimentoResponse> obter() { return ResponseEntity.ok(service.obter()); }

    @GetMapping("/slug-disponivel")
    public ResponseEntity<com.marquify.beta.response.SlugDisponivelResponse> verificarSlug(@RequestParam String slug) {
        return ResponseEntity.ok(service.verificarSlug(slug));
    }

    @PutMapping
    public ResponseEntity<ConfiguracoesEstabelecimentoResponse> salvar(@Valid @RequestBody ConfiguracoesEstabelecimentoRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }
}
