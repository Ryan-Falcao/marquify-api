package com.marquify.beta.response;

import com.marquify.beta.entity.Profissional;

import java.time.Instant;

public record ProfissionalResponse(Long id, String nome, boolean ativo, Instant criadoEm, Instant atualizadoEm) {
    public static ProfissionalResponse from(Profissional profissional) {
        return new ProfissionalResponse(profissional.getId(), profissional.getNome(), profissional.isAtivo(),
                profissional.getCriadoEm(), profissional.getAtualizadoEm());
    }
}
