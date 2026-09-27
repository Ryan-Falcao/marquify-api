package com.marquify.beta.controllers;

import com.marquify.beta.response.EstabelecimentoPublicoResponse;
import com.marquify.beta.response.ProfissionalPublicoResponse;
import com.marquify.beta.response.ServicoResponse;
import com.marquify.beta.response.HorariosLivresResponse;
import com.marquify.beta.service.DisponibilidadeService;
import com.marquify.beta.service.CatalogoPublicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/publico/e/{codigoPublico}")
@RequiredArgsConstructor
public class EstabelecimentoPublicoController {
    private final CatalogoPublicoService catalogo;
    private final DisponibilidadeService disponibilidade;

    @GetMapping
    public ResponseEntity<EstabelecimentoPublicoResponse> estabelecimento(@PathVariable String codigoPublico) {
        return ResponseEntity.ok(catalogo.estabelecimento(codigoPublico));
    }

    @GetMapping("/servicos")
    public ResponseEntity<List<ServicoResponse>> servicos(@PathVariable String codigoPublico) {
        return ResponseEntity.ok(catalogo.servicos(codigoPublico));
    }

    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalPublicoResponse>> profissionais(@PathVariable String codigoPublico) {
        return ResponseEntity.ok(catalogo.profissionais(codigoPublico));
    }

    @GetMapping("/profissionais/{profissionalId}/horarios")
    public ResponseEntity<HorariosLivresResponse> horarios(@PathVariable String codigoPublico,
                                                             @PathVariable Long profissionalId,
                                                             @RequestParam Long servicoId,
                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(disponibilidade.horariosLivres(
                catalogo.idDoEstabelecimento(codigoPublico), profissionalId, servicoId, data));
    }
}
