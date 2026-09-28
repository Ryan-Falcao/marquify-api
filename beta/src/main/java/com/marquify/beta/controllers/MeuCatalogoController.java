package com.marquify.beta.controllers;

import com.marquify.beta.request.PosicaoFotoRequest;
import com.marquify.beta.request.ServicoCatalogoRequest;
import com.marquify.beta.response.ServicoResponse;
import com.marquify.beta.service.ServicoFotoService;
import com.marquify.beta.service.VendedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vendedor/me/servicos")
@RequiredArgsConstructor
public class MeuCatalogoController {
    private final VendedorService vendedorService;
    private final ServicoFotoService fotos;

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar() {
        return ResponseEntity.ok(vendedorService.listarMeusServicos());
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@RequestBody @Valid ServicoCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendedorService.criarServicoAtual(request));
    }

    @PutMapping("/{servicoId}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long servicoId,
                                                       @RequestBody @Valid ServicoCatalogoRequest request) {
        return ResponseEntity.ok(vendedorService.atualizarServicoAtual(servicoId, request));
    }

    @PatchMapping("/{servicoId}/ativar")
    public ResponseEntity<ServicoResponse> ativar(@PathVariable Long servicoId) {
        return ResponseEntity.ok(vendedorService.ativarServicoAtual(servicoId));
    }

    @PatchMapping("/{servicoId}/desativar")
    public ResponseEntity<ServicoResponse> desativar(@PathVariable Long servicoId) {
        return ResponseEntity.ok(vendedorService.desativarServicoAtual(servicoId));
    }

    @PatchMapping("/{servicoId}/arquivar")
    public ResponseEntity<ServicoResponse> arquivar(@PathVariable Long servicoId) {
        return ResponseEntity.ok(vendedorService.arquivarServicoAtual(servicoId));
    }

    @PatchMapping("/{servicoId}/restaurar")
    public ResponseEntity<ServicoResponse> restaurar(@PathVariable Long servicoId) {
        return ResponseEntity.ok(vendedorService.restaurarServicoAtual(servicoId));
    }

    @PutMapping(path = "/{servicoId}/foto", consumes = "multipart/form-data")
    public ResponseEntity<ServicoResponse> foto(@PathVariable Long servicoId,
                                                 @RequestPart("foto") MultipartFile foto) {
        return ResponseEntity.ok(ServicoResponse.from(fotos.salvarAtual(servicoId, foto)));
    }

    @PatchMapping("/{servicoId}/foto-posicao")
    public ResponseEntity<ServicoResponse> posicionarFoto(@PathVariable Long servicoId,
                                                           @RequestBody @Valid PosicaoFotoRequest posicao) {
        return ResponseEntity.ok(ServicoResponse.from(fotos.posicionarAtual(servicoId, posicao)));
    }
}
