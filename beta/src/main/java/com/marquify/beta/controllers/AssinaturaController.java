package com.marquify.beta.controllers;

import com.marquify.beta.response.AssinaturaResponse;
import com.marquify.beta.response.CheckoutAssinaturaResponse;
import com.marquify.beta.service.AssinaturaService;
import com.marquify.beta.service.MercadoPagoAssinaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendedor/me/assinatura")
@RequiredArgsConstructor
public class AssinaturaController {
    private final AssinaturaService assinaturas;
    private final MercadoPagoAssinaturaService mercadoPago;

    @GetMapping
    public ResponseEntity<AssinaturaResponse> consultar() {
        return ResponseEntity.ok(assinaturas.minhaAssinatura());
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutAssinaturaResponse> checkout() {
        return ResponseEntity.status(HttpStatus.CREATED).body(mercadoPago.iniciarCheckout());
    }
}
