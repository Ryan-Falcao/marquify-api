package com.marquify.beta.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonalizacaoCatalogoRequest(
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 180) String descricao,
        @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}") String corPrimaria,
        @Size(max = 2_800_000) String logo,
        @Size(max = 2_800_000) String capa,
        @Min(0) @Max(100) int capaPosicaoX,
        @Min(0) @Max(100) int capaPosicaoY,
        @Size(max = 4000) String tema
) {}
