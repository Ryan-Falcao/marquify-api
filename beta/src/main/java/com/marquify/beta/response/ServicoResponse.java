package com.marquify.beta.response;

import com.marquify.beta.entity.Servicos;
import java.time.LocalTime;

public record ServicoResponse(Long id, String nome, String descricao, Double preco,
                              LocalTime tempo, Long vendedorId) {
    public static ServicoResponse from(Servicos servico) {
        return new ServicoResponse(servico.getId(), servico.getNome(), servico.getDescricao(),
                servico.getPreco(), servico.getTempo(), servico.getVendedor().getId());
    }
}
