package com.marquify.beta.controllers;


import com.marquify.beta.response.AgendamentoResponse;
import com.marquify.beta.response.ServicoResponse;
import com.marquify.beta.response.VendedorResponse;
import com.marquify.beta.response.DashboardResponse;
import com.marquify.beta.response.LinkAgendamentoResponse;
import com.marquify.beta.request.ServicoRequest;
import com.marquify.beta.request.VendedorRequest;
import com.marquify.beta.service.VendedorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import java.util.List;


@RestController
@RequestMapping("/vendedor")
@AllArgsConstructor
public class VendedorController {

    private final VendedorService vendedorService;

    @GetMapping("/{id}")
    public ResponseEntity<VendedorResponse> getMyInfos(@PathVariable Long id){
        return ResponseEntity.ok(vendedorService.getMyInfos(id));
    }
    @GetMapping("/me")
    public ResponseEntity<VendedorResponse> getMyInfos(){
        return ResponseEntity.ok(vendedorService.getMyInfos());
    }
    @GetMapping("/me/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(vendedorService.dashboard());
    }
    @GetMapping("/me/link-agendamento")
    public ResponseEntity<LinkAgendamentoResponse> linkAgendamento() {
        return ResponseEntity.ok(vendedorService.linkAgendamento());
    }
    @GetMapping("/agendamentos")
    public ResponseEntity<List<AgendamentoResponse>> getAgendamentos(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.getAgendamentos(request));
    }
    @GetMapping("/{id}/agendamentos")
    public ResponseEntity<List<AgendamentoResponse>> getAgendamentosDoVendedor(@PathVariable Long id){
        return ResponseEntity.ok(vendedorService.getAgendamentos(id));
    }
    @GetMapping("/me/agendamentos")
    public ResponseEntity<List<AgendamentoResponse>> getMeusAgendamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        if (inicio == null && fim == null) return ResponseEntity.ok(vendedorService.getAgendamentos());
        return ResponseEntity.ok(vendedorService.getAgendamentos(inicio, fim));
    }
    @PutMapping("/mudarNome")
    public ResponseEntity<VendedorResponse> mudarNome(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarNome(request));
    }
    @PutMapping("/mudarNomeLoja")
    public ResponseEntity<VendedorResponse> mudarNomeLoja(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarNomeLoja(request));
    }
    @PutMapping("/mudarDiasAbertos")
    public ResponseEntity<VendedorResponse> mudarDiasAbertos(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarDiasAbertos(request));
    }
    @PutMapping("/mudarHoraAbertura")
    public ResponseEntity<VendedorResponse> mudarHoraAbertura(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarHoraAbertura(request));
    }
    @PutMapping("/mudarHoraFechamento")
    public ResponseEntity<VendedorResponse>  mudarHoraFechamento(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarHoraFechamento(request));
    }
    @PostMapping("/criarServico")
    public ResponseEntity<ServicoResponse>  criarServico(@RequestBody ServicoRequest request){
        return ResponseEntity.ok(vendedorService.criarServico(request));
    }
    @DeleteMapping("/deletarServico")
    public ResponseEntity<Void> deletarServico(@RequestBody ServicoRequest request){
        vendedorService.deletarServico(request);
        return ResponseEntity.noContent().build();
    }
}

