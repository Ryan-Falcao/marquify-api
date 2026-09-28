package com.marquify.beta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "assinaturas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false, unique = true)
    private Estabelecimento estabelecimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanoAssinatura plano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAssinatura status;

    @Column(name = "teste_gratis_ate")
    private Instant testeGratisAte;

    @Column(name = "mercado_pago_assinatura_id", length = 120)
    private String mercadoPagoAssinaturaId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    private Assinatura(Estabelecimento estabelecimento, PlanoAssinatura plano, StatusAssinatura status, Instant testeGratisAte) {
        this.estabelecimento = estabelecimento;
        this.plano = plano;
        this.status = status;
        this.testeGratisAte = testeGratisAte;
    }

    public static Assinatura testeGratis(Estabelecimento estabelecimento, Instant terminaEm) {
        return new Assinatura(estabelecimento, PlanoAssinatura.PROFISSIONAL, StatusAssinatura.TESTE_GRATIS, terminaEm);
    }

    public static Assinatura gratuita(Estabelecimento estabelecimento) {
        return new Assinatura(estabelecimento, PlanoAssinatura.GRATUITO, StatusAssinatura.ATIVA, null);
    }

    public boolean testeGratisAtivo(Instant agora) {
        return status == StatusAssinatura.TESTE_GRATIS && testeGratisAte != null && testeGratisAte.isAfter(agora);
    }

    public boolean profissionalAtivo(Instant agora) {
        return plano == PlanoAssinatura.PROFISSIONAL
                && (status == StatusAssinatura.ATIVA || testeGratisAtivo(agora));
    }

    public void expirarTeste() {
        if (status == StatusAssinatura.TESTE_GRATIS) status = StatusAssinatura.EXPIRADA;
    }

    public void iniciarCobrancaMercadoPago(String assinaturaId) {
        this.mercadoPagoAssinaturaId = assinaturaId;
        this.status = StatusAssinatura.PENDENTE;
    }

    /** O teste só passa a existir depois de o Mercado Pago confirmar o cartão do assinante. */
    public void iniciarTesteGratisAposCartao(Instant terminaEm) {
        if (status == StatusAssinatura.TESTE_GRATIS || status == StatusAssinatura.ATIVA && plano == PlanoAssinatura.PROFISSIONAL) return;
        this.plano = PlanoAssinatura.PROFISSIONAL;
        this.status = StatusAssinatura.TESTE_GRATIS;
        this.testeGratisAte = terminaEm;
    }

    public void ativarProfissional() {
        this.plano = PlanoAssinatura.PROFISSIONAL;
        this.status = StatusAssinatura.ATIVA;
        this.testeGratisAte = null;
    }

    public void cancelar() {
        this.status = StatusAssinatura.CANCELADA;
    }

    @PrePersist
    private void registrarCriacao() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    private void registrarAtualizacao() {
        atualizadoEm = Instant.now();
    }
}
