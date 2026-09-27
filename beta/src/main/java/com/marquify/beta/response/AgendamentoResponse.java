package com.marquify.beta.response;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Status;
import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoResponse(Long id, LocalDate data, LocalTime horaInicio, LocalTime horaFim,
                                  Status status, ClienteResumo cliente, VendedorResumo vendedor,
                                  EstabelecimentoResumo estabelecimento, ProfissionalResumo profissional,
                                  ServicoResponse servico, java.math.BigDecimal valorCobrado, long duracaoMinutos) {
    public record ClienteResumo(Long id, String nome) {}
    public record VendedorResumo(Long id, String nomeLoja) {}
    public record EstabelecimentoResumo(Long id, String nome, String fusoHorario) {}
    public record ProfissionalResumo(Long id, String nome) {}

    public static AgendamentoResponse from(Agendamento agendamento) {
        var cliente = agendamento.getCliente();
        var vendedor = agendamento.getVendedor();
        var estabelecimento = agendamento.getEstabelecimento();
        var profissional = agendamento.getProfissional();
        return new AgendamentoResponse(agendamento.getId(), agendamento.getData(), agendamento.getHoraInicio(),
                agendamento.getHoraFim(), agendamento.getStatus(),
                cliente == null ? null : new ClienteResumo(cliente.getId(), cliente.getNome()),
                vendedor == null ? null : new VendedorResumo(vendedor.getId(), vendedor.getNomeLoja()),
                estabelecimento == null ? null : new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome(),
                        estabelecimento.getFusoHorario()),
                profissional == null ? null : new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                agendamento.getServico() == null ? null : ServicoResponse.from(agendamento.getServico()),
                agendamento.getValorCobrado(), java.time.Duration.between(agendamento.getHoraInicio(), agendamento.getHoraFim()).toMinutes());
    }
}
