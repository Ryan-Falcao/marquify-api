package com.marquify.beta.controllers;
import com.marquify.beta.request.*;
import com.marquify.beta.response.*;
import com.marquify.beta.service.ConviteProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor public class ConviteProfissionalController {
 private final ConviteProfissionalService service;
 @PostMapping("/vendedor/me/profissionais/{id}/convite") public ResponseEntity<ConviteProfissionalResponse> convidar(@PathVariable Long id, @RequestBody @Valid ConviteProfissionalRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.convidar(id, request)); }
 @GetMapping("/auth/convites-profissional/{token}") public ResponseEntity<ConviteProfissionalResponse> consultar(@PathVariable String token) { return ResponseEntity.ok(service.consultar(token)); }
 @PostMapping("/auth/convites-profissional/{token}/aceitar") public ResponseEntity<LoginResponse> aceitar(@PathVariable String token, @RequestBody @Valid AceitarConviteProfissionalRequest request) { return ResponseEntity.ok(service.aceitar(token, request)); }
}
