package com.marquify.beta.controllers;
import com.marquify.beta.request.*;
import com.marquify.beta.response.*;
import com.marquify.beta.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/profissional/me") @RequiredArgsConstructor public class AgendaProfissionalController {
 private final DisponibilidadeService disponibilidade; private final BloqueioAgendaService bloqueios;
 @GetMapping("/disponibilidade") public ResponseEntity<DisponibilidadeResponse> disponibilidade(){return ResponseEntity.ok(disponibilidade.consultarDoProfissional());}
 @PutMapping("/disponibilidade") public ResponseEntity<DisponibilidadeResponse> salvar(@RequestBody @Valid DisponibilidadeRequest request){return ResponseEntity.ok(disponibilidade.substituirDoProfissional(request));}
 @GetMapping("/bloqueios") public ResponseEntity<List<BloqueioAgendaResponse>> bloqueios(@RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate inicio,@RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fim){return ResponseEntity.ok(bloqueios.listarDoProfissional(inicio,fim));}
 @PostMapping("/bloqueios") public ResponseEntity<BloqueioAgendaResponse> criar(@RequestBody @Valid BloqueioAgendaRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(bloqueios.criarDoProfissional(request));}
 @DeleteMapping("/bloqueios/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id){bloqueios.excluirDoProfissional(id);return ResponseEntity.noContent().build();}
}
