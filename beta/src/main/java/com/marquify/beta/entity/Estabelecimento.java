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
import java.util.UUID;

@Entity
@Table(name = "estabelecimentos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_publico", nullable = false, unique = true, updatable = false, length = 36)
    private String codigoPublico;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(name = "fuso_horario", nullable = false, length = 63)
    private String fusoHorario;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "descricao_publica", length = 180)
    private String descricaoPublica;

    @Column(name = "cor_primaria", length = 7)
    private String corPrimaria;

    @Column(name = "logo_publico", columnDefinition = "TEXT")
    private String logoPublico;

    @Column(name = "capa_publica", columnDefinition = "TEXT")
    private String capaPublica;

    @Column(name = "capa_posicao_x")
    private Integer capaPosicaoX;

    @Column(name = "capa_posicao_y")
    private Integer capaPosicaoY;

    @Column(name = "tema_publico", columnDefinition = "TEXT")
    private String temaPublico;

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

    public void personalizarPaginaPublica(String nome, String descricao, String corPrimaria,
                                          String logoPublico, String capaPublica,
                                          int capaPosicaoX, int capaPosicaoY, String temaPublico) {
        alterarNome(nome);
        if (descricao != null && descricao.length() > 180) throw new IllegalArgumentException("Descrição deve ter até 180 caracteres");
        if (corPrimaria == null || !corPrimaria.matches("#[0-9A-Fa-f]{6}")) throw new IllegalArgumentException("Cor principal inválida");
        validarImagemPublica(logoPublico);
        validarImagemPublica(capaPublica);
        if (capaPosicaoX < 0 || capaPosicaoX > 100 || capaPosicaoY < 0 || capaPosicaoY > 100) throw new IllegalArgumentException("Posição da capa inválida");
        this.descricaoPublica = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.corPrimaria = corPrimaria.toUpperCase();
        this.logoPublico = logoPublico;
        this.capaPublica = capaPublica;
        this.capaPosicaoX = capaPosicaoX;
        this.capaPosicaoY = capaPosicaoY;
        if (temaPublico != null && temaPublico.length() > 4000) throw new IllegalArgumentException("Configuração visual é muito grande");
        this.temaPublico = temaPublico;
    }

    private static void validarImagemPublica(String imagem) {
        if (imagem == null) return;
        if (imagem.length() > 2_800_000 || !imagem.matches("^data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/=\\r\\n]+$")) {
            throw new IllegalArgumentException("Imagem pública inválida");
        }
    }

    @PrePersist
    private void registrarCriacao() {
        if (codigoPublico == null) {
            codigoPublico = UUID.randomUUID().toString();
        }
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    private void registrarAtualizacao() {
        this.atualizadoEm = Instant.now();
    }
}
