package com.marquify.beta.controllers;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.request.AgendamentoRequest;
import com.marquify.beta.service.Agendamentoservice;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agendamento")
@AllArgsConstructor
public class AgendamentoController {

    private final Agendamentoservice agendamentoservice;

    @PostMapping
    public ResponseEntity<Agendamento> agendar(@RequestBody AgendamentoRequest request){
        return ResponseEntity.ok(agendamentoservice.agendar(request));
    }
    @PutMapping("/cancelar")
    public ResponseEntity<Agendamento> cancelar(@RequestBody AgendamentoRequest request){
        return ResponseEntity.ok(agendamentoservice.cancelar(request));
    }
}
