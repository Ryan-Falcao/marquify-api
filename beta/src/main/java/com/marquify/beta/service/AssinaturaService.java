package com.marquify.beta.service;

import com.marquify.beta.entity.Assinatura;
import com.marquify.beta.entity.Estabelecimento;
import com.marquify.beta.entity.PlanoAssinatura;
import com.marquify.beta.entity.StatusAssinatura;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.AssinaturaRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.agendamentoRepository;
import com.marquify.beta.repository.servicoRepository;
import com.marquify.beta.response.AssinaturaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AssinaturaService {
    public static final int DIAS_TESTE_GRATIS = 14;
    private static final int LIMITE_SERVICOS_GRATUITO = 5;
    private static final int LIMITE_PROFISSIONAIS_GRATUITO = 1;
    private static final int LIMITE_AGENDAMENTOS_GRATUITO = 30;

    private final AssinaturaRepository assinaturas;
    private final servicoRepository servicos;
    private final ProfissionalRepository profissionais;
    private final agendamentoRepository agendamentos;
    private final CurrentUser currentUser;

    public void criarAssinaturaGratuita(Estabelecimento estabelecimento) {
        assinaturas.save(Assinatura.gratuita(estabelecimento));
    }

    @Transactional(readOnly = true)
    public AssinaturaResponse minhaAssinatura() {
        Vendedor vendedor = currentUser.vendedor();
        return resposta(obter(vendedor.getEstabelecimento().getId()), vendedor.getEstabelecimento().getId(), Instant.now());
    }

    public void validarNovoServico(Long estabelecimentoId) {
        Assinatura assinatura = obterParaAlteracao(estabelecimentoId);
        if (profissional(assinatura, Instant.now())) return;
        if (servicos.countByEstabelecimentoIdAndAtivoTrue(estabelecimentoId) >= LIMITE_SERVICOS_GRATUITO) {
            throw limite("O plano gratuito permite até " + LIMITE_SERVICOS_GRATUITO + " serviços ativos. Assine o Profissional para continuar.");
        }
    }

    public void validarNovoProfissional(Long estabelecimentoId) {
        Assinatura assinatura = obterParaAlteracao(estabelecimentoId);
        if (profissional(assinatura, Instant.now())) return;
        if (profissionais.countByEstabelecimentoIdAndAtivoTrue(estabelecimentoId) >= LIMITE_PROFISSIONAIS_GRATUITO) {
            throw limite("O plano gratuito permite 1 profissional ativo. Assine o Profissional para adicionar a equipe.");
        }
    }

    public void validarNovoAgendamento(Long estabelecimentoId) {
        Assinatura assinatura = obterParaAlteracao(estabelecimentoId);
        Instant agora = Instant.now();
        if (profissional(assinatura, agora)) return;
        Instant inicioDoMes = agora.atZone(ZoneOffset.UTC).withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        if (agendamentos.countByEstabelecimentoIdAndCriadoEmGreaterThanEqual(estabelecimentoId, inicioDoMes) >= LIMITE_AGENDAMENTOS_GRATUITO) {
            throw limite("Este estabelecimento atingiu o limite mensal de agendamentos do plano gratuito.");
        }
    }

    public Assinatura assinaturaParaCheckout(Long estabelecimentoId) {
        return obterParaAlteracao(estabelecimentoId);
    }

    public void iniciarTesteAposCartaoMercadoPago(String mercadoPagoAssinaturaId) {
        assinaturas.findByMercadoPagoAssinaturaId(mercadoPagoAssinaturaId)
                .ifPresent(assinatura -> assinatura.iniciarTesteGratisAposCartao(Instant.now().plus(DIAS_TESTE_GRATIS, ChronoUnit.DAYS)));
    }

    public void cancelarPorMercadoPago(String mercadoPagoAssinaturaId) {
        assinaturas.findByMercadoPagoAssinaturaId(mercadoPagoAssinaturaId).ifPresent(Assinatura::cancelar);
    }

    private AssinaturaResponse resposta(Assinatura assinatura, Long estabelecimentoId, Instant agora) {
        boolean profissional = profissional(assinatura, agora);
        long dias = assinatura.testeGratisAtivo(agora)
                ? Math.max(1, Math.ceilDiv(Duration.between(agora, assinatura.getTesteGratisAte()).toSeconds(), 86_400)) : 0;
        Instant inicioDoMes = agora.atZone(ZoneOffset.UTC).withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        return new AssinaturaResponse(
                profissional ? PlanoAssinatura.PROFISSIONAL : PlanoAssinatura.GRATUITO,
                !profissional && assinatura.getStatus() == StatusAssinatura.TESTE_GRATIS
                        ? StatusAssinatura.EXPIRADA : assinatura.getStatus(),
                assinatura.getTesteGratisAte(),
                dias,
                profissional ? new AssinaturaResponse.LimitesPlano(-1, -1, -1)
                        : new AssinaturaResponse.LimitesPlano(LIMITE_SERVICOS_GRATUITO, LIMITE_PROFISSIONAIS_GRATUITO, LIMITE_AGENDAMENTOS_GRATUITO),
                new AssinaturaResponse.UsoPlano(
                        servicos.countByEstabelecimentoIdAndAtivoTrue(estabelecimentoId),
                        profissionais.countByEstabelecimentoIdAndAtivoTrue(estabelecimentoId),
                        agendamentos.countByEstabelecimentoIdAndCriadoEmGreaterThanEqual(estabelecimentoId, inicioDoMes)
                )
        );
    }

    private boolean profissional(Assinatura assinatura, Instant agora) {
        return assinatura.profissionalAtivo(agora);
    }

    private Assinatura obter(Long estabelecimentoId) {
        return assinaturas.findByEstabelecimentoId(estabelecimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Assinatura não encontrada para este estabelecimento"));
    }

    private Assinatura obterParaAlteracao(Long estabelecimentoId) {
        return assinaturas.findByEstabelecimentoIdParaAlteracao(estabelecimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Assinatura não encontrada para este estabelecimento"));
    }

    private ResponseStatusException limite(String mensagem) {
        return new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, mensagem);
    }
}
