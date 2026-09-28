package com.marquify.beta.controllers;

import com.marquify.beta.service.MercadoPagoAssinaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pagamentos/mercado-pago")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {
    private final MercadoPagoAssinaturaService mercadoPago;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload,
                                        @RequestHeader(value = "x-signature", required = false) String assinatura,
                                        @RequestHeader(value = "x-request-id", required = false) String requestId) {
        Object data = payload.get("data");
        String id = data instanceof Map<?, ?> mapa && mapa.get("id") != null ? String.valueOf(mapa.get("id")) : null;
        if (id != null) mercadoPago.processarWebhook(id, assinatura, requestId);
        return ResponseEntity.noContent().build();
    }
}
