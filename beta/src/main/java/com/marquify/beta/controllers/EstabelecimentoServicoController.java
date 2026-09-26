package com.marquify.beta.controllers;

import com.marquify.beta.request.ServicoCatalogoRequest;
import com.marquify.beta.response.ServicoResponse;
import com.marquify.beta.service.VendedorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estabelecimentos/{estabelecimentoId}/servicos")
@AllArgsConstructor
public class EstabelecimentoServicoController {
    private final VendedorService vendedorService;

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar(@PathVariable Long estabelecimentoId) {
        return ResponseEntity.ok(vendedorService.listarServicos(estabelecimentoId));
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@PathVariable Long estabelecimentoId,
                                                   @RequestBody @Valid ServicoCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendedorService.criarServicoNoEstabelecimento(estabelecimentoId, request));
    }

    @PutMapping("/{servicoId}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long estabelecimentoId,
                                                       @PathVariable Long servicoId,
                                                       @RequestBody @Valid ServicoCatalogoRequest request) {
        return ResponseEntity.ok(vendedorService.atualizarServico(estabelecimentoId, servicoId, request));
    }

    @PatchMapping("/{servicoId}/ativar")
    public ResponseEntity<ServicoResponse> ativar(@PathVariable Long estabelecimentoId,
                                                    @PathVariable Long servicoId) {
        return ResponseEntity.ok(vendedorService.ativarServico(estabelecimentoId, servicoId));
    }

    @PatchMapping("/{servicoId}/desativar")
    public ResponseEntity<ServicoResponse> desativar(@PathVariable Long estabelecimentoId,
                                                       @PathVariable Long servicoId) {
        return ResponseEntity.ok(vendedorService.desativarServico(estabelecimentoId, servicoId));
    }
}
