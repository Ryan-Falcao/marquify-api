package com.marquify.beta.service;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Servicos;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.repository.agendamentoRepository;
import com.marquify.beta.repository.servicoRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.ServicoRequest;
import com.marquify.beta.request.VendedorRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VendedorService {

    private final vendedorRepository vendedorRepository;
    private final agendamentoRepository agendamentoRepository;
    private final servicoRepository servicoRepository;

    public  List<Agendamento> getAgendamentos(VendedorRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedor_id())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        List<Agendamento> agendamentos = agendamentoRepository.findByVendedorId(request.getVendedor_id());

        return agendamentos;
    }

    public Vendedor getMyInfos(Long id){
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        return vendedor;
    }

    public Vendedor mudarNome(VendedorRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedor_id())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        vendedor.setNome(request.getNewNome());
        return vendedorRepository.save(vendedor);
    }

    public  Vendedor mudarNomeLoja (VendedorRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedor_id())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        vendedor.setNomeLoja(request.getNewNomeLoja());
        return vendedorRepository.save(vendedor);
    }
    public Vendedor mudarDiasAbertos(VendedorRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedor_id())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        vendedor.setDiasAbertos(request.getNewDiasAbertos());
        return vendedorRepository.save(vendedor);
    }

    public Vendedor mudarHoraAbertura(VendedorRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedor_id())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        vendedor.setHoraAbertura(request.getNewHoraAbertura());
        return vendedorRepository.save(vendedor);
    }

    public Vendedor mudarHoraFechamento(VendedorRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedor_id())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        vendedor.setHoraFechamento(request.getNewHoraFechamento());
        return vendedorRepository.save(vendedor);
    }
    public Servicos criarServico(ServicoRequest request){
        Vendedor vendedor = vendedorRepository.findById(request.getVendedorId())
                .orElseThrow(()-> new RuntimeException("Vendedor nao encontrado"));
        Servicos servico = new Servicos();

        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setPreco(request.getPreco());
        servico.setTempo(request.getTempo());
        servico.setVendedor(vendedor);

        return servicoRepository.save(servico);
    }

    public void deletarServico(ServicoRequest request){
        Servicos servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(()-> new RuntimeException("Servico nao existe"));
        Vendedor vendedor = vendedorRepository.findById(request.getVendedorId())
                .orElseThrow(()-> new RuntimeException("Id invalido"));

        servicoRepository.deleteById(request.getServicoId());
    }
}
