package com.marquify.beta.controllers;

import com.marquify.beta.service.ServicoFotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/publico/servicos")
@RequiredArgsConstructor
public class ServicoFotoPublicaController {
    private final ServicoFotoService fotos;
    @GetMapping("/{servicoId}/foto")
    public ResponseEntity<Resource> foto(@PathVariable Long servicoId) {
        Resource foto = fotos.carregar(servicoId);
        MediaType tipo = foto.getFilename() != null && foto.getFilename().endsWith(".png")
                ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo)
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS)).body(foto);
    }
}
