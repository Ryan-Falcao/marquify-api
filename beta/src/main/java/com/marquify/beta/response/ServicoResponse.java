package com.marquify.beta.response;

import com.marquify.beta.entity.Servicos;
import java.time.LocalTime;
import java.util.Set;

public record ServicoResponse(Long id, String nome, String descricao, Double preco,
                              LocalTime tempo, boolean ativo, Long estabelecimentoId, Long vendedorId,
                              Set<ProfissionalResumo> profissionais, String fotoUrl,
                              int fotoPosicaoX, int fotoPosicaoY) {
    public record ProfissionalResumo(Long id, String nome) {}

    public static ServicoResponse from(Servicos servico) {
        return new ServicoResponse(servico.getId(), servico.getNome(), servico.getDescricao(),
                servico.getPreco(), servico.getTempo(), servico.isAtivo(), servico.getEstabelecimento().getId(),
                servico.getVendedor() == null ? null : servico.getVendedor().getId(),
                servico.getProfissionais().stream()
                        .map(profissional -> new ProfissionalResumo(profissional.getId(), profissional.getNome()))
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()),
                servico.getFotoArquivo() == null ? null : "/publico/servicos/" + servico.getId() + "/foto",
                servico.getFotoPosicaoX(), servico.getFotoPosicaoY());
    }
}
