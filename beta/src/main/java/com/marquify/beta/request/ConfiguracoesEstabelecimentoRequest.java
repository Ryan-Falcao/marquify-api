package com.marquify.beta.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record ConfiguracoesEstabelecimentoRequest(
        @NotBlank @Size(max = 160) String nome,
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "A URL só pode usar letras minúsculas, números e hífens") @Size(min = 3, max = 60) String slugPublico,
        @Size(max = 180) String descricao,
        @Size(max = 30) String telefone,
        @Size(max = 255) String endereco,
        @NotBlank @Size(max = 63) String fusoHorario,
        @Min(0) int antecedenciaMinimaMinutos,
        @Min(1) @Max(365) int janelaMaximaAgendamentoDias,
        @Min(0) @Max(720) int intervaloEntreServicosMinutos,
        @Min(0) int antecedenciaCancelamentoMinutos
) {}
