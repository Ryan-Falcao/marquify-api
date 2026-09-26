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
        Profissional profissional = new Profissional(vendedor.getEstabelecimento(), request.nome());
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
        Profissional profissional = profissionalDoEstabelecimento(vendedorId, profissionalId);
        profissional.desativar();
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

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }
}
