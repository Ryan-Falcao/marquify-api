package com.marquify.beta.response;

import com.marquify.beta.entity.Estabelecimento;

public record ConfiguracoesEstabelecimentoResponse(String nome, String slugPublico, String descricao, String telefone, String endereco,
                                                    String fusoHorario, int antecedenciaMinimaMinutos,
                                                    int janelaMaximaAgendamentoDias, int intervaloEntreServicosMinutos,
                                                    int antecedenciaCancelamentoMinutos) {
    public static ConfiguracoesEstabelecimentoResponse from(Estabelecimento estabelecimento) {
        return new ConfiguracoesEstabelecimentoResponse(estabelecimento.getNome(), estabelecimento.getSlugPublico(), estabelecimento.getDescricaoPublica(),
                estabelecimento.getTelefone(), estabelecimento.getEndereco(), estabelecimento.getFusoHorario(),
                estabelecimento.getAntecedenciaMinimaMinutos(), estabelecimento.getJanelaMaximaAgendamentoDias(),
                estabelecimento.getIntervaloEntreServicosMinutos(), estabelecimento.getAntecedenciaCancelamentoMinutos());
    }
}
