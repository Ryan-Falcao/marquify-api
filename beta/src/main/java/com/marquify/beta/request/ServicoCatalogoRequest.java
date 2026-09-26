package com.marquify.beta.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.Set;

public record ServicoCatalogoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 160, message = "Nome deve ter no máximo 160 caracteres")
        String nome,
        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,
        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
        Double preco,
        @NotNull(message = "Duração é obrigatória")
        LocalTime tempo,
        @NotEmpty(message = "Informe ao menos um profissional")
        Set<Long> profissionaisIds
) {
}
