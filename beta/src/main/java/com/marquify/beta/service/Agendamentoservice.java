package com.marquify.beta.service;

import com.marquify.beta.entity.*;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.*;
import com.marquify.beta.request.AgendamentoRequest;
import com.marquify.beta.response.AgendamentoResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Transactional
public class Agendamentoservice {
    private final agendamentoRepository agendamentos;
    private final servicoRepository servicos;
    private final vendedorRepository vendedores;
    private final ProfissionalRepository profissionais;
    private final DisponibilidadeService disponibilidade;
    private final CurrentUser currentUser;

    public AgendamentoResponse agendar(AgendamentoRequest request) {
        Cliente cliente = currentUser.cliente();
        if (request.getClienteId() != null && !request.getClienteId().equals(cliente.getId())) {
            throw new AccessDeniedException("Acesso negado");
        }
        if (request.getServicoId() == null || request.getVendedorId() == null || request.getProfissionalId() == null
                || request.getData() == null || request.getHoraInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Serviço, vendedor, profissional, data e horário são obrigatórios");
        }
        Vendedor vendedor = vendedores.findById(request.getVendedorId()).orElseThrow(this::notFound);
        Servicos servico = servicos.findByIdAndEstabelecimentoIdAndAtivoTrue(request.getServicoId(), vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
        if (servico.getTempo() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço sem duração configurada");
        }
        Profissional profissional = profissionais.findByIdAndEstabelecimentoIdAndAtivoTrue(
                request.getProfissionalId(), servico.getEstabelecimento().getId()).orElseThrow(this::notFound);
        if (!servico.executadoPor(profissional.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Profissional não oferece o serviço selecionado");
        }
        if (!disponibilidade.horarioDisponivel(profissional, servico, request.getData(), request.getHoraInicio())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário indisponível");
        }
        Agendamento agendamento = new Agendamento();
        agendamento.setData(request.getData());
        agendamento.setHoraInicio(request.getHoraInicio());
        agendamento.setHoraFim(request.getHoraInicio().plusHours(servico.getTempo().getHour())
                .plusMinutes(servico.getTempo().getMinute()));
        agendamento.setStatus(Status.AGENDADO);
        agendamento.setCliente(cliente);
        agendamento.setVendedor(vendedor);
        agendamento.vincularEstabelecimento(servico.getEstabelecimento());
        agendamento.vincularProfissional(profissional);
        agendamento.setServico(servico);
        return AgendamentoResponse.from(agendamentos.save(agendamento));
    }

    public AgendamentoResponse cancelar(AgendamentoRequest request) {
        if (request.getAgendamentoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agendamento é obrigatório");
        }
        Object principal = currentUser.principal();
        Agendamento agendamento;
        if (principal instanceof Cliente cliente) {
            agendamento = agendamentos.findByIdAndClienteId(request.getAgendamentoId(), cliente.getId())
                    .orElseThrow(this::notFound);
        } else if (principal instanceof Vendedor vendedor && vendedor.getRole() == UserRole.ADMIN) {
            agendamento = agendamentos.findByIdAndVendedorId(request.getAgendamentoId(), vendedor.getId())
                    .orElseThrow(this::notFound);
        } else {
            throw new AccessDeniedException("Acesso negado");
        }
        agendamento.setStatus(Status.CANCELADO);
        return AgendamentoResponse.from(agendamentos.save(agendamento));
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }
}
