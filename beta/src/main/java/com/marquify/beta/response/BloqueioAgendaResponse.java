package com.marquify.beta.response;

import com.marquify.beta.entity.BloqueioAgenda;
import com.marquify.beta.entity.TipoBloqueioAgenda;

import java.time.LocalDate;
import java.time.LocalTime;

public record BloqueioAgendaResponse(Long id, Long profissionalId, TipoBloqueioAgenda tipo,
                                     LocalDate dataInicio, LocalDate dataFim,
                                     LocalTime horaInicio, LocalTime horaFim, String motivo, boolean recorrente) {
    public static BloqueioAgendaResponse from(BloqueioAgenda bloqueio) {
        return new BloqueioAgendaResponse(bloqueio.getId(), bloqueio.getProfissional().getId(), bloqueio.getTipo(),
                bloqueio.getDataInicio(), bloqueio.getDataFim(), bloqueio.getHoraInicio(), bloqueio.getHoraFim(),
                bloqueio.getMotivo(), bloqueio.isRecorrente());
    }
}
