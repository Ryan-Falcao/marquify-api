package com.marquify.beta.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfissionalRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 160, message = "Nome deve ter no máximo 160 caracteres")
        String nome
) {
}
