package com.marquify.beta.controllers;

import com.marquify.beta.response.AgendamentoResponse;
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
    public ResponseEntity<AgendamentoResponse> agendar(@RequestBody AgendamentoRequest request){
        return ResponseEntity.ok(agendamentoservice.agendar(request));
    }
    @PutMapping("/cancelar")
    public ResponseEntity<AgendamentoResponse> cancelar(@RequestBody AgendamentoRequest request){
        return ResponseEntity.ok(agendamentoservice.cancelar(request));
    }
}
