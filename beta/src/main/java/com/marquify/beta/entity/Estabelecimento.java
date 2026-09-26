package com.marquify.beta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.DateTimeException;
import java.time.ZoneId;

@Entity
@Table(name = "estabelecimentos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(name = "fuso_horario", nullable = false, length = 63)
    private String fusoHorario;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public Estabelecimento(String nome, String fusoHorario) {
        alterarNome(nome);
        alterarFusoHorario(fusoHorario);
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank() || nome.length() > 160) {
            throw new IllegalArgumentException("Nome do estabelecimento deve ter entre 1 e 160 caracteres");
        }
        this.nome = nome.trim();
    }

    public void alterarFusoHorario(String fusoHorario) {
        if (fusoHorario == null || fusoHorario.isBlank()) {
            throw new IllegalArgumentException("Fuso horário do estabelecimento é obrigatório");
        }
        try {
            this.fusoHorario = ZoneId.of(fusoHorario).getId();
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("Fuso horário do estabelecimento é inválido", exception);
        }
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    @PrePersist
    private void registrarCriacao() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    private void registrarAtualizacao() {
        this.atualizadoEm = Instant.now();
    }
}
