package com.marquify.beta.controllers;

import com.marquify.beta.response.HorariosLivresResponse;
import com.marquify.beta.service.DisponibilidadeService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/publico/estabelecimentos/{estabelecimentoId}/profissionais/{profissionalId}/horarios")
@AllArgsConstructor
public class DisponibilidadePublicaController {
    private final DisponibilidadeService disponibilidadeService;

    @GetMapping
    public ResponseEntity<HorariosLivresResponse> horarios(@PathVariable Long estabelecimentoId,
                                                             @PathVariable Long profissionalId,
                                                             @RequestParam Long servicoId,
                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(disponibilidadeService.horariosLivres(estabelecimentoId, profissionalId, servicoId, data));
    }
}
