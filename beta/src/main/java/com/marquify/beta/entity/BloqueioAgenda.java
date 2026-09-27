package com.marquify.beta.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bloqueios_agenda")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BloqueioAgenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoBloqueioAgenda tipo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean recorrente;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(length = 180)
    private String motivo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    public BloqueioAgenda(Profissional profissional, TipoBloqueioAgenda tipo, LocalDate dataInicio,
                          LocalDate dataFim, LocalTime horaInicio, LocalTime horaFim, String motivo,
                          boolean recorrente) {
        if (profissional == null || tipo == null || dataInicio == null
                || (dataFim != null && dataFim.isBefore(dataInicio))) {
            throw new IllegalArgumentException("Período do bloqueio é inválido");
        }
        if ((horaInicio == null) != (horaFim == null)
                || (horaInicio != null && !horaInicio.isBefore(horaFim))) {
            throw new IllegalArgumentException("Horário do bloqueio é inválido");
        }
        if (tipo == TipoBloqueioAgenda.PAUSA && horaInicio == null) {
            throw new IllegalArgumentException("Pausa deve possuir horário");
        }
        if (tipo == TipoBloqueioAgenda.PAUSA && !recorrente
                && (dataFim == null || !dataInicio.equals(dataFim))) {
            throw new IllegalArgumentException("Pausa pontual deve ocorrer em uma única data");
        }
        if (tipo != TipoBloqueioAgenda.PAUSA && recorrente) {
            throw new IllegalArgumentException("Recorrência diária é permitida somente para pausas");
        }
        if (tipo != TipoBloqueioAgenda.PAUSA && dataFim == null) {
            throw new IllegalArgumentException("Data final é obrigatória");
        }
        this.profissional = profissional;
        this.tipo = tipo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.motivo = motivo == null || motivo.isBlank() ? null : motivo.trim();
        this.recorrente = recorrente;
        this.criadoEm = Instant.now();
    }

    public boolean bloqueia(LocalDate data, LocalTime inicio, LocalTime fim) {
        if (data.isBefore(dataInicio) || (dataFim != null && data.isAfter(dataFim))) return false;
        return horaInicio == null || (inicio.isBefore(horaFim) && horaInicio.isBefore(fim));
    }
}
