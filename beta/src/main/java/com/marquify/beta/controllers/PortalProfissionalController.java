package com.marquify.beta.controllers;
import com.marquify.beta.request.StatusAtendimentoRequest;
import com.marquify.beta.response.*;
import com.marquify.beta.service.PortalProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/profissional/me") @RequiredArgsConstructor public class PortalProfissionalController {
 private final PortalProfissionalService service;
 @GetMapping public ResponseEntity<ProfissionalResponse> perfil(){return ResponseEntity.ok(service.perfil());}
 @GetMapping("/agenda") public ResponseEntity<List<AgendamentoResponse>> agenda(@RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate data){return ResponseEntity.ok(service.agenda(data));}
 @PatchMapping("/agenda/{id}/status") public ResponseEntity<AgendamentoResponse> status(@PathVariable Long id,@RequestBody @Valid StatusAtendimentoRequest request){return ResponseEntity.ok(service.alterarStatus(id,request));}
}
