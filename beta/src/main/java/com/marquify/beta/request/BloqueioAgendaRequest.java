package com.marquify.beta.request;

import com.marquify.beta.entity.TipoBloqueioAgenda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record BloqueioAgendaRequest(
        @NotNull TipoBloqueioAgenda tipo,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim,
        LocalTime horaInicio,
        LocalTime horaFim,
        @Size(max = 180) String motivo,
        boolean recorrente
) {}
