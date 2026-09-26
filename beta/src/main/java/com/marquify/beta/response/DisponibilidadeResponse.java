package com.marquify.beta.response;

import com.marquify.beta.entity.DisponibilidadeProfissional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadeResponse(Long profissionalId, List<Jornada> jornadas) {
    public record Jornada(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        static Jornada from(DisponibilidadeProfissional disponibilidade) {
            return new Jornada(disponibilidade.getDiaSemana(), disponibilidade.getHoraInicio(), disponibilidade.getHoraFim());
        }
    }

    public static DisponibilidadeResponse from(Long profissionalId, List<DisponibilidadeProfissional> disponibilidades) {
        return new DisponibilidadeResponse(profissionalId, disponibilidades.stream().map(Jornada::from).toList());
    }
}
