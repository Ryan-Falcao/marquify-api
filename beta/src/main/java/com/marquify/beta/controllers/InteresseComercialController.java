package com.marquify.beta.controllers;

import com.marquify.beta.entity.InteresseComercial;
import com.marquify.beta.repository.InteresseComercialRepository;
import com.marquify.beta.request.InteresseComercialRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interesses")
@AllArgsConstructor
public class InteresseComercialController {
    private final InteresseComercialRepository interesses;

    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody @Valid InteresseComercialRequest request) {
        interesses.save(new InteresseComercial(request.nome(), request.estabelecimento(), request.email(),
                request.whatsapp(), request.segmento()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
