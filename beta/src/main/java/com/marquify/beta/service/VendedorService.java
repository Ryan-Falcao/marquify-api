package com.marquify.beta.service;

import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.entity.Servicos;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.*;
import com.marquify.beta.request.*;
import com.marquify.beta.response.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class VendedorService {
    private final vendedorRepository vendedores;
    private final agendamentoRepository agendamentos;
    private final servicoRepository servicos;
    private final CurrentUser currentUser;

    private Vendedor ownVendedor(Long id) {
        currentUser.vendedor(id);
        return vendedores.findById(id).orElseThrow(this::notFound);
    }

    @Transactional(readOnly = true)
    public VendedorResponse getMyInfos(Long id) {
        return VendedorResponse.from(ownVendedor(id));
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> getAgendamentos(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        return agendamentos.findByVendedorId(vendedor.getId()).stream().map(AgendamentoResponse::from).toList();
    }

    public VendedorResponse mudarNome(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewNome() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setNome(request.getNewNome());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarNomeLoja(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewNomeLoja() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setNomeLoja(request.getNewNomeLoja());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarDiasAbertos(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewDiasAbertos() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setDiasAbertos(request.getNewDiasAbertos());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarHoraAbertura(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewHoraAbertura() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setHoraAbertura(request.getNewHoraAbertura());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarHoraFechamento(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewHoraFechamento() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setHoraFechamento(request.getNewHoraFechamento());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public ServicoResponse criarServico(ServicoRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedorId());
        Servicos servico = new Servicos();
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setPreco(request.getPreco());
        servico.setTempo(request.getTempo());
        servico.setVendedor(vendedor);
        return ServicoResponse.from(servicos.save(servico));
    }

    public void deletarServico(ServicoRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedorId());
        if (request.getServicoId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço é obrigatório");
        Servicos servico = servicos.findByIdAndVendedorId(request.getServicoId(), vendedor.getId())
                .orElseThrow(this::notFound);
        servicos.delete(servico);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }
}
