package com.marquify.beta.service;

import com.marquify.beta.entity.Estabelecimento;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.request.ConfiguracoesEstabelecimentoRequest;
import com.marquify.beta.response.ConfiguracoesEstabelecimentoResponse;
import com.marquify.beta.response.SlugDisponivelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfiguracoesEstabelecimentoService {
    private static final Set<String> SLUGS_RESERVADOS = Set.of("admin", "api", "auth", "agendar", "login", "cadastro", "configuracoes", "publico", "suporte", "marquify", "www");
    private final CurrentUser currentUser;
    private final EstabelecimentoRepository estabelecimentos;

    private Estabelecimento estabelecimentoAtual() {
        Long id = currentUser.vendedor().getEstabelecimento().getId();
        return estabelecimentos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado"));
    }

    @Transactional(readOnly = true)
    public ConfiguracoesEstabelecimentoResponse obter() {
        return ConfiguracoesEstabelecimentoResponse.from(estabelecimentoAtual());
    }

    @Transactional(readOnly = true)
    public SlugDisponivelResponse verificarSlug(String slug) {
        if (slug == null || !slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*") || slug.length() < 3 || slug.length() > 60) {
            return new SlugDisponivelResponse(false, "Use de 3 a 60 caracteres: letras minúsculas, números e hífens");
        }
        if (SLUGS_RESERVADOS.contains(slug)) return new SlugDisponivelResponse(false, "Esta URL é reservada");
        boolean emUso = estabelecimentos.existsBySlugPublicoAndIdNot(slug, estabelecimentoAtual().getId());
        return new SlugDisponivelResponse(!emUso, emUso ? "Esta URL já está em uso" : "URL disponível");
    }

    public ConfiguracoesEstabelecimentoResponse salvar(ConfiguracoesEstabelecimentoRequest request) {
        Estabelecimento estabelecimento = estabelecimentoAtual();
        try {
            if (request.slugPublico() != null && !request.slugPublico().equals(estabelecimento.getSlugPublico())) {
                if (SLUGS_RESERVADOS.contains(request.slugPublico())) {
                    throw new IllegalArgumentException("Esta URL é reservada. Escolha outro nome");
                }
                if (estabelecimentos.existsBySlugPublicoAndIdNot(request.slugPublico(), estabelecimento.getId())) {
                    throw new IllegalArgumentException("Esta URL já está em uso. Escolha outra");
                }
                estabelecimento.alterarSlugPublico(request.slugPublico());
            }
            estabelecimento.configurar(request.nome(), request.descricao(), request.telefone(), request.endereco(),
                    request.fusoHorario(), request.antecedenciaMinimaMinutos(), request.janelaMaximaAgendamentoDias(),
                    request.intervaloEntreServicosMinutos(), request.antecedenciaCancelamentoMinutos());
            return ConfiguracoesEstabelecimentoResponse.from(estabelecimentos.save(estabelecimento));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
