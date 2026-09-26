package com.marquify.beta.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidades_profissionais")
@Getter
@NoArgsConstructor
public class DisponibilidadeProfissional {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemana;

    private LocalTime horaInicio;
    private LocalTime horaFim;

    public DisponibilidadeProfissional(Profissional profissional, DayOfWeek diaSemana,
                                       LocalTime horaInicio, LocalTime horaFim) {
        if (profissional == null || diaSemana == null || horaInicio == null || horaFim == null
                || !horaInicio.isBefore(horaFim)) {
            throw new IllegalArgumentException("Jornada do profissional é inválida");
        }
        this.profissional = profissional;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }
}
