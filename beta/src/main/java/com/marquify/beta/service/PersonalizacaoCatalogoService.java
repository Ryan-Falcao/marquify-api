package com.marquify.beta.service;

import com.marquify.beta.entity.Estabelecimento;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.request.PersonalizacaoCatalogoRequest;
import com.marquify.beta.response.EstabelecimentoPublicoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalizacaoCatalogoService {
    private final CurrentUser currentUser;
    private final EstabelecimentoRepository estabelecimentos;

    private Estabelecimento estabelecimentoAtual() {
        Long id = currentUser.vendedor().getEstabelecimento().getId();
        return estabelecimentos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Estabelecimento não encontrado"));
    }

    @Transactional(readOnly = true)
    public EstabelecimentoPublicoResponse obter() {
        return EstabelecimentoPublicoResponse.from(estabelecimentoAtual());
    }

    public EstabelecimentoPublicoResponse salvar(PersonalizacaoCatalogoRequest request) {
        Estabelecimento estabelecimento = estabelecimentoAtual();
        try {
            estabelecimento.personalizarPaginaPublica(request.nome(), request.descricao(), request.corPrimaria(),
                    request.logo(), request.capa(), request.capaPosicaoX(), request.capaPosicaoY(), request.tema());
            return EstabelecimentoPublicoResponse.from(estabelecimentos.save(estabelecimento));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
