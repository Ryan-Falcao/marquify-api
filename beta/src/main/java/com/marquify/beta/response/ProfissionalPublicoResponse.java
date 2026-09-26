package com.marquify.beta.response;

import com.marquify.beta.entity.Profissional;

public record ProfissionalPublicoResponse(Long id, String nome) {
    public static ProfissionalPublicoResponse from(Profissional profissional) {
        return new ProfissionalPublicoResponse(profissional.getId(), profissional.getNome());
    }
}
