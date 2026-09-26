package com.marquify.beta.controllers;

import com.marquify.beta.response.EstabelecimentoPublicoResponse;
import com.marquify.beta.response.ProfissionalPublicoResponse;
import com.marquify.beta.response.ServicoResponse;
import com.marquify.beta.service.CatalogoPublicoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/publico/estabelecimentos/{estabelecimentoId}")
@AllArgsConstructor
public class CatalogoPublicoController {
    private final CatalogoPublicoService catalogo;

    @GetMapping
    public ResponseEntity<EstabelecimentoPublicoResponse> estabelecimento(@PathVariable Long estabelecimentoId) {
        return ResponseEntity.ok(catalogo.estabelecimento(estabelecimentoId));
    }

    @GetMapping("/servicos")
    public ResponseEntity<List<ServicoResponse>> servicos(@PathVariable Long estabelecimentoId) {
        return ResponseEntity.ok(catalogo.servicos(estabelecimentoId));
    }

    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalPublicoResponse>> profissionais(@PathVariable Long estabelecimentoId) {
        return ResponseEntity.ok(catalogo.profissionais(estabelecimentoId));
    }
}
