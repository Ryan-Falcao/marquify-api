package com.marquify.beta.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InteresseComercialRequest(
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Size(max = 160) String estabelecimento,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 30) String whatsapp,
        @NotBlank @Size(max = 80) String segmento
) {}
