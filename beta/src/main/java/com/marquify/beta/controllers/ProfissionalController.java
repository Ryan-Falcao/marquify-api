package com.marquify.beta.controllers;

import com.marquify.beta.request.ProfissionalRequest;
import com.marquify.beta.request.DisponibilidadeRequest;
import com.marquify.beta.response.DisponibilidadeResponse;
import com.marquify.beta.response.ProfissionalResponse;
import com.marquify.beta.service.DisponibilidadeService;
import com.marquify.beta.service.ProfissionalService;
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
@RequestMapping("/vendedor/{vendedorId}/profissionais")
@AllArgsConstructor
public class ProfissionalController {
    private final ProfissionalService profissionalService;
    private final DisponibilidadeService disponibilidadeService;

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listar(@PathVariable Long vendedorId) {
        return ResponseEntity.ok(profissionalService.listar(vendedorId));
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> criar(@PathVariable Long vendedorId,
                                                        @RequestBody @Valid ProfissionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profissionalService.criar(vendedorId, request));
    }

    @PutMapping("/{profissionalId}")
    public ResponseEntity<ProfissionalResponse> atualizar(@PathVariable Long vendedorId,
                                                            @PathVariable Long profissionalId,
                                                            @RequestBody @Valid ProfissionalRequest request) {
        return ResponseEntity.ok(profissionalService.atualizar(vendedorId, profissionalId, request));
    }

    @PatchMapping("/{profissionalId}/ativar")
    public ResponseEntity<ProfissionalResponse> ativar(@PathVariable Long vendedorId,
                                                         @PathVariable Long profissionalId) {
        return ResponseEntity.ok(profissionalService.ativar(vendedorId, profissionalId));
    }

    @PatchMapping("/{profissionalId}/desativar")
    public ResponseEntity<ProfissionalResponse> desativar(@PathVariable Long vendedorId,
                                                            @PathVariable Long profissionalId) {
        return ResponseEntity.ok(profissionalService.desativar(vendedorId, profissionalId));
    }

    @GetMapping("/{profissionalId}/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> disponibilidade(@PathVariable Long vendedorId,
                                                                     @PathVariable Long profissionalId) {
        return ResponseEntity.ok(disponibilidadeService.consultarGestao(vendedorId, profissionalId));
    }

    @PutMapping("/{profissionalId}/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> substituirDisponibilidade(@PathVariable Long vendedorId,
                                                                               @PathVariable Long profissionalId,
                                                                               @RequestBody @Valid DisponibilidadeRequest request) {
        return ResponseEntity.ok(disponibilidadeService.substituir(vendedorId, profissionalId, request));
    }
}
