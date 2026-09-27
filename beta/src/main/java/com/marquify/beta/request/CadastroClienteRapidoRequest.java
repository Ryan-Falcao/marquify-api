package com.marquify.beta.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroClienteRapidoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 32) String numero,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 72) String senha
) {}
