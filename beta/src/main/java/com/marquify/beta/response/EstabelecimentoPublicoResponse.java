package com.marquify.beta.response;

import com.marquify.beta.entity.Estabelecimento;

public record EstabelecimentoPublicoResponse(Long id, String slugPublico, String nome, String fusoHorario, String descricaoPublica,
                                             String corPrimaria, String logoPublico, String capaPublica,
                                             int capaPosicaoX, int capaPosicaoY, String temaPublico) {
    public static EstabelecimentoPublicoResponse from(Estabelecimento estabelecimento) {
        return new EstabelecimentoPublicoResponse(estabelecimento.getId(), estabelecimento.getSlugPublico(), estabelecimento.getNome(),
                estabelecimento.getFusoHorario(), estabelecimento.getDescricaoPublica(),
                estabelecimento.getCorPrimaria(), estabelecimento.getLogoPublico(), estabelecimento.getCapaPublica(),
                estabelecimento.getCapaPosicaoX() == null ? 50 : estabelecimento.getCapaPosicaoX(),
                estabelecimento.getCapaPosicaoY() == null ? 50 : estabelecimento.getCapaPosicaoY(),
                estabelecimento.getTemaPublico());
    }
}
