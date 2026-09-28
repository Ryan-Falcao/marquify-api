package com.marquify.beta.service;

import com.marquify.beta.entity.Profissional;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.ProfissionalRequest;
import com.marquify.beta.response.ProfissionalResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class ProfissionalService {
    private final vendedorRepository vendedores;
    private final ProfissionalRepository profissionais;
    private final CurrentUser currentUser;
    private final AssinaturaService assinaturas;

    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listar(Long vendedorId) {
        Vendedor vendedor = vendedorDoUsuario(vendedorId);
        return profissionais.findAllByEstabelecimentoIdOrderByNomeAsc(vendedor.getEstabelecimento().getId()).stream()
                .map(ProfissionalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfissionalResponse> listarAtual() {
        Vendedor atual = currentUser.vendedor();
        Vendedor vendedor = vendedores.findById(atual.getId()).orElseThrow(this::notFound);
        return profissionais.findAllByEstabelecimentoIdOrderByNomeAsc(vendedor.getEstabelecimento().getId()).stream()
                .map(ProfissionalResponse::from).toList();
    }

    public ProfissionalResponse criar(Long vendedorId, ProfissionalRequest request) {
        Vendedor vendedor = vendedorDoUsuario(vendedorId);
        assinaturas.validarNovoProfissional(vendedor.getEstabelecimento().getId());
        Profissional profissional = new Profissional(vendedor.getEstabelecimento(), request.nome());
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse criarAtual(ProfissionalRequest request) {
        Vendedor vendedor = vendedorAtual();
        assinaturas.validarNovoProfissional(vendedor.getEstabelecimento().getId());
        return ProfissionalResponse.from(profissionais.save(new Profissional(vendedor.getEstabelecimento(), request.nome())));
    }

    public ProfissionalResponse atualizarAtual(Long profissionalId, ProfissionalRequest request) {
        Profissional profissional = profissionalDoEstabelecimentoAtual(profissionalId);
        profissional.alterarNome(request.nome());
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse ativarAtual(Long profissionalId) {
        Profissional profissional = profissionalDoEstabelecimentoAtual(profissionalId);
        profissional.ativar();
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse desativarAtual(Long profissionalId) {
        return arquivarAtual(profissionalId);
    }

    public ProfissionalResponse arquivarAtual(Long profissionalId) {
        Profissional profissional = profissionalDoEstabelecimentoAtual(profissionalId);
        profissional.arquivar();
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse restaurarAtual(Long profissionalId) {
        Profissional profissional = profissionalDoEstabelecimentoAtual(profissionalId);
        if (!profissional.isAtivo()) assinaturas.validarNovoProfissional(profissional.getEstabelecimento().getId());
        profissional.restaurar();
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse atualizar(Long vendedorId, Long profissionalId, ProfissionalRequest request) {
        Profissional profissional = profissionalDoEstabelecimento(vendedorId, profissionalId);
        profissional.alterarNome(request.nome());
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse ativar(Long vendedorId, Long profissionalId) {
        Profissional profissional = profissionalDoEstabelecimento(vendedorId, profissionalId);
        profissional.ativar();
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse desativar(Long vendedorId, Long profissionalId) {
        return arquivar(vendedorId, profissionalId);
    }

    public ProfissionalResponse arquivar(Long vendedorId, Long profissionalId) {
        Profissional profissional = profissionalDoEstabelecimento(vendedorId, profissionalId);
        profissional.arquivar();
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    public ProfissionalResponse restaurar(Long vendedorId, Long profissionalId) {
        Profissional profissional = profissionalDoEstabelecimento(vendedorId, profissionalId);
        if (!profissional.isAtivo()) assinaturas.validarNovoProfissional(profissional.getEstabelecimento().getId());
        profissional.restaurar();
        return ProfissionalResponse.from(profissionais.save(profissional));
    }

    private Profissional profissionalDoEstabelecimento(Long vendedorId, Long profissionalId) {
        Vendedor vendedor = vendedorDoUsuario(vendedorId);
        return profissionais.findByIdAndEstabelecimentoId(profissionalId, vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
    }

    private Vendedor vendedorDoUsuario(Long vendedorId) {
        currentUser.vendedor(vendedorId);
        return vendedores.findById(vendedorId).orElseThrow(this::notFound);
    }

    private Vendedor vendedorAtual() {
        Vendedor atual = currentUser.vendedor();
        return vendedores.findById(atual.getId()).orElseThrow(this::notFound);
    }

    private Profissional profissionalDoEstabelecimentoAtual(Long profissionalId) {
        Vendedor vendedor = vendedorAtual();
        return profissionais.findByIdAndEstabelecimentoId(profissionalId, vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }
}
