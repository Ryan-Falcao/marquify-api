package com.marquify.beta.response;

import com.marquify.beta.entity.Estabelecimento;

public record EstabelecimentoPublicoResponse(Long id, String nome, String fusoHorario) {
    public static EstabelecimentoPublicoResponse from(Estabelecimento estabelecimento) {
        return new EstabelecimentoPublicoResponse(estabelecimento.getId(), estabelecimento.getNome(),
                estabelecimento.getFusoHorario());
    }
}
