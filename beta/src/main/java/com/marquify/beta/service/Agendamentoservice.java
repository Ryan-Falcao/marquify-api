package com.marquify.beta.service;

import com.marquify.beta.entity.*;
import com.marquify.beta.repository.agendamentoRepository;
import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.servicoRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.AgendamentoRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@AllArgsConstructor
public class Agendamentoservice {

    private agendamentoRepository agendamentoRepository;
    private clienteRepository clienteRepository;
    private servicoRepository servicoRepository;
    private vendedorRepository vendedorRepository;

    public Agendamento agendar(AgendamentoRequest request){
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado"));
        Servicos servicos = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado"));
        Vendedor vendedor = vendedorRepository.findById(request.getVendedorId())
                .orElseThrow(() -> new RuntimeException("Vendedor nao encontrado"));

        Agendamento agendamento = new Agendamento();

        LocalTime horaFim = request.getHoraInicio()
                .plusHours(servicos.getTempo().getHour())
                .plusMinutes(servicos.getTempo().getMinute());

        agendamento.setData(request.getData());
        agendamento.setHoraInicio(request.getHoraInicio());
        agendamento.setHoraFim(horaFim);
        agendamento.setStatus(Status.AGENDADO);
        agendamento.setCliente(cliente);
        agendamento.setVendedor(vendedor);
        agendamento.setServico(servicos);

        return agendamentoRepository.save(agendamento);
    }

    public Agendamento cancelar(AgendamentoRequest request){
        Agendamento agendamento = agendamentoRepository.findById(request.getAgendamentoId())
                .orElseThrow(()-> new RuntimeException("Agendamento nao encontrado"));

        agendamento.setStatus(Status.CANCELADO);

        return agendamentoRepository.save(agendamento);
    }
}
