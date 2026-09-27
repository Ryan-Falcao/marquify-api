package com.marquify.beta.controllers;

import com.marquify.beta.response.AgendamentoResponse;
import com.marquify.beta.service.Agendamentoservice;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.response.ClienteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cliente/me")
@RequiredArgsConstructor
public class ClienteController {
    private final Agendamentoservice agendamentos;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<ClienteResponse> perfil() {
        return ResponseEntity.ok(ClienteResponse.from(currentUser.cliente()));
    }

    @GetMapping("/agendamentos")
    public ResponseEntity<List<AgendamentoResponse>> agendamentos() {
        return ResponseEntity.ok(agendamentos.meusAgendamentos());
    }
}
