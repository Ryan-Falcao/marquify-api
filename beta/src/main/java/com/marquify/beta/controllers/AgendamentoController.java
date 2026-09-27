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

    @PutMapping("/{id}/remarcar")
    public ResponseEntity<AgendamentoResponse> remarcar(@PathVariable Long id, @RequestBody AgendamentoRequest request) {
        return ResponseEntity.ok(agendamentoservice.remarcar(id, request.getData(), request.getHoraInicio()));
    }

    @GetMapping("/{id}/horarios-remarcacao")
    public ResponseEntity<java.util.List<java.time.LocalTime>> horarios(@PathVariable Long id,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate data) {
        return ResponseEntity.ok(agendamentoservice.horariosRemarcacao(id, data));
    }
}
