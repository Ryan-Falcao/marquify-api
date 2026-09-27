package com.marquify.beta.service;

import com.marquify.beta.entity.Estabelecimento;
import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.servicoRepository;
import com.marquify.beta.response.EstabelecimentoPublicoResponse;
import com.marquify.beta.response.ProfissionalPublicoResponse;
import com.marquify.beta.response.ServicoResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CatalogoPublicoService {
    private final EstabelecimentoRepository estabelecimentos;
    private final servicoRepository servicos;
    private final ProfissionalRepository profissionais;

    public EstabelecimentoPublicoResponse estabelecimento(Long estabelecimentoId) {
        return EstabelecimentoPublicoResponse.from(estabelecimentoAtivo(estabelecimentoId));
    }

    public EstabelecimentoPublicoResponse estabelecimento(String codigoPublico) {
        return EstabelecimentoPublicoResponse.from(estabelecimentoAtivo(codigoPublico));
    }

    public List<ServicoResponse> servicos(Long estabelecimentoId) {
        estabelecimentoAtivo(estabelecimentoId);
        return servicos.findAllByEstabelecimentoIdOrderByNomeAsc(estabelecimentoId).stream()
                .filter(servico -> servico.isAtivo())
                .map(ServicoResponse::from)
                .toList();
    }

    public List<ProfissionalPublicoResponse> profissionais(Long estabelecimentoId) {
        estabelecimentoAtivo(estabelecimentoId);
        return profissionais.findAllByEstabelecimentoIdAndAtivoTrue(estabelecimentoId).stream()
                .map(ProfissionalPublicoResponse::from)
                .toList();
    }

    public List<ServicoResponse> servicos(String codigoPublico) {
        return servicos(estabelecimentoAtivo(codigoPublico).getId());
    }

    public List<ProfissionalPublicoResponse> profissionais(String codigoPublico) {
        return profissionais(estabelecimentoAtivo(codigoPublico).getId());
    }

    public Long idDoEstabelecimento(String codigoPublico) {
        return estabelecimentoAtivo(codigoPublico).getId();
    }

    private Estabelecimento estabelecimentoAtivo(Long estabelecimentoId) {
        return estabelecimentos.findByIdAndAtivoTrue(estabelecimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado"));
    }

    private Estabelecimento estabelecimentoAtivo(String codigoPublico) {
        return estabelecimentos.findByCodigoPublicoAndAtivoTrue(codigoPublico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado"));
    }
}
