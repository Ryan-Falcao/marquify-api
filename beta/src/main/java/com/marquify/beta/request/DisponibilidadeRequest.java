package com.marquify.beta.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadeRequest(@NotEmpty List<@Valid Jornada> jornadas) {
    public record Jornada(@NotNull DayOfWeek diaSemana, @NotNull LocalTime horaInicio, @NotNull LocalTime horaFim) {}
}
