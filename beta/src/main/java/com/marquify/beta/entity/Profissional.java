package com.marquify.beta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Recurso que executa serviços e terá uma agenda própria. Não representa uma conta de acesso. */
@Entity
@Table(name = "profissionais")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public Profissional(Estabelecimento estabelecimento, String nome) {
        vincularEstabelecimento(estabelecimento);
        alterarNome(nome);
    }

    public void vincularEstabelecimento(Estabelecimento estabelecimento) {
        if (estabelecimento == null) {
            throw new IllegalArgumentException("Estabelecimento é obrigatório");
        }
        this.estabelecimento = estabelecimento;
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank() || nome.length() > 160) {
            throw new IllegalArgumentException("Nome do profissional deve ter entre 1 e 160 caracteres");
        }
        this.nome = nome.trim();
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
