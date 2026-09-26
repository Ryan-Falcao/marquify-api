package com.marquify.beta.controllers;

import com.marquify.beta.response.ProfissionalResponse;
import com.marquify.beta.service.ProfissionalService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vendedor/me/profissionais")
@AllArgsConstructor
public class MinhaEquipeController {
    private final ProfissionalService profissionais;

    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listar() {
        return ResponseEntity.ok(profissionais.listarAtual());
    }
}
