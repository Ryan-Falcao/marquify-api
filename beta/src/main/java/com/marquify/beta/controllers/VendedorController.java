package com.marquify.beta.controllers;


import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Servicos;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.request.ServicoRequest;
import com.marquify.beta.request.VendedorRequest;
import com.marquify.beta.service.VendedorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/vendedor")
@AllArgsConstructor
public class VendedorController {

    private final VendedorService vendedorService;

    @GetMapping("/{id}")
    public ResponseEntity<Vendedor> getMyInfos(@PathVariable Long id){
        return ResponseEntity.ok(vendedorService.getMyInfos(id));
    }
    @GetMapping("/agendamentos")
    public ResponseEntity<List<Agendamento>> getAgendamentos(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.getAgendamentos(request));
    }
    @PutMapping("/mudarNome")
    public ResponseEntity<Vendedor> mudarNome(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarNome(request));
    }
    @PutMapping("/mudarNomeLoja")
    public ResponseEntity<Vendedor> mudarNomeLoja(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarNomeLoja(request));
    }
    @PutMapping("/mudarDiasAbertos")
    public ResponseEntity<Vendedor> mudarDiasAbertos(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarDiasAbertos(request));
    }
    @PutMapping("/mudarHoraAbertura")
    public ResponseEntity<Vendedor> mudarHoraAbertura(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarHoraAbertura(request));
    }
    @PutMapping("/mudarHoraFechamento")
    public ResponseEntity<Vendedor>  mudarHoraFechamento(@RequestBody VendedorRequest request){
        return ResponseEntity.ok(vendedorService.mudarHoraFechamento(request));
    }
    @PostMapping("/criarServico")
    public ResponseEntity<Servicos>  criarServico(@RequestBody ServicoRequest request){
        return ResponseEntity.ok(vendedorService.criarServico(request));
    }
    @DeleteMapping("/deletarServico")
    public ResponseEntity<Void> deletarServico(@RequestBody ServicoRequest request){
        vendedorService.deletarServico(request);
        return ResponseEntity.noContent().build();
    }
}

