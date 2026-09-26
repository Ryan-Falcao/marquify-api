package com.marquify.beta.response;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Status;
import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoResponse(Long id, LocalDate data, LocalTime horaInicio, LocalTime horaFim,
                                  Status status, ClienteResumo cliente, VendedorResumo vendedor,
                                  ServicoResponse servico) {
    public record ClienteResumo(Long id, String nome) {}
    public record VendedorResumo(Long id, String nomeLoja) {}

    public static AgendamentoResponse from(Agendamento agendamento) {
        var cliente = agendamento.getCliente();
        var vendedor = agendamento.getVendedor();
        return new AgendamentoResponse(agendamento.getId(), agendamento.getData(), agendamento.getHoraInicio(),
                agendamento.getHoraFim(), agendamento.getStatus(),
                cliente == null ? null : new ClienteResumo(cliente.getId(), cliente.getNome()),
                vendedor == null ? null : new VendedorResumo(vendedor.getId(), vendedor.getNomeLoja()),
                agendamento.getServico() == null ? null : ServicoResponse.from(agendamento.getServico()));
    }
}
