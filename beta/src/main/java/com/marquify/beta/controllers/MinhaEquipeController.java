package com.marquify.beta.controllers;

import com.marquify.beta.response.ProfissionalResponse;
import com.marquify.beta.service.ProfissionalService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import com.marquify.beta.request.ProfissionalRequest;
import com.marquify.beta.request.DisponibilidadeRequest;
import com.marquify.beta.response.DisponibilidadeResponse;
import com.marquify.beta.service.DisponibilidadeService;
import jakarta.validation.Valid;
import com.marquify.beta.request.BloqueioAgendaRequest;
import com.marquify.beta.response.BloqueioAgendaResponse;
import com.marquify.beta.service.BloqueioAgendaService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/vendedor/me/profissionais")
@AllArgsConstructor
public class MinhaEquipeController {
    private final ProfissionalService profissionais;
    private final DisponibilidadeService disponibilidades;
    private final BloqueioAgendaService bloqueios;

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listar() {
        return ResponseEntity.ok(profissionais.listarAtual());
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> criar(@RequestBody @Valid ProfissionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profissionais.criarAtual(request));
    }

    @PutMapping("/{profissionalId}")
    public ResponseEntity<ProfissionalResponse> atualizar(@PathVariable Long profissionalId,
                                                            @RequestBody @Valid ProfissionalRequest request) {
        return ResponseEntity.ok(profissionais.atualizarAtual(profissionalId, request));
    }

    @PatchMapping("/{profissionalId}/ativar")
    public ResponseEntity<ProfissionalResponse> ativar(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(profissionais.ativarAtual(profissionalId));
    }

    @PatchMapping("/{profissionalId}/desativar")
    public ResponseEntity<ProfissionalResponse> desativar(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(profissionais.desativarAtual(profissionalId));
    }

    @PatchMapping("/{profissionalId}/arquivar")
    public ResponseEntity<ProfissionalResponse> arquivar(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(profissionais.arquivarAtual(profissionalId));
    }

    @PatchMapping("/{profissionalId}/restaurar")
    public ResponseEntity<ProfissionalResponse> restaurar(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(profissionais.restaurarAtual(profissionalId));
    }

    @GetMapping("/{profissionalId}/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> disponibilidade(@PathVariable Long profissionalId) {
        return ResponseEntity.ok(disponibilidades.consultarGestaoAtual(profissionalId));
    }

    @PutMapping("/{profissionalId}/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> substituirDisponibilidade(@PathVariable Long profissionalId,
                                                                               @RequestBody @Valid DisponibilidadeRequest request) {
        return ResponseEntity.ok(disponibilidades.substituirAtual(profissionalId, request));
    }

    @GetMapping("/{profissionalId}/bloqueios")
    public ResponseEntity<List<BloqueioAgendaResponse>> listarBloqueios(
            @PathVariable Long profissionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(bloqueios.listarAtual(profissionalId, inicio, fim));
    }

    @PostMapping("/{profissionalId}/bloqueios")
    public ResponseEntity<BloqueioAgendaResponse> criarBloqueio(@PathVariable Long profissionalId,
                                                                  @RequestBody @Valid BloqueioAgendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueios.criarAtual(profissionalId, request));
    }

    @DeleteMapping("/{profissionalId}/bloqueios/{bloqueioId}")
    public ResponseEntity<Void> excluirBloqueio(@PathVariable Long profissionalId, @PathVariable Long bloqueioId) {
        bloqueios.excluirAtual(profissionalId, bloqueioId);
        return ResponseEntity.noContent().build();
    }
}
