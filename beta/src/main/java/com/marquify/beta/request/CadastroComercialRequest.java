package com.marquify.beta.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroComercialRequest(
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Size(max = 160) String estabelecimento,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @NotBlank @Size(max = 63) String fusoHorario
) {}
