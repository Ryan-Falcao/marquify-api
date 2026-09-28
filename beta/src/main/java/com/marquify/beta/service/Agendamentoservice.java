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
import java.util.List;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.math.BigDecimal;

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
    private final AssinaturaService assinaturas;

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
        assinaturas.validarNovoAgendamento(vendedor.getEstabelecimento().getId());
        validarMomentoFuturo(request.getData(), request.getHoraInicio(), vendedor);
        Servicos servico = servicos.findByIdAndEstabelecimentoIdAndAtivoTrue(request.getServicoId(), vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
        if (servico.getTempo() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço sem duração configurada");
        }
        Profissional profissional = profissionais.findAtivoDoEstabelecimentoParaReserva(
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
                .plusMinutes(servico.getTempo().getMinute()).plusSeconds(servico.getTempo().getSecond()));
        agendamento.setValorCobrado(BigDecimal.valueOf(servico.getPreco()));
        agendamento.setStatus(Status.AGENDADO);
        agendamento.setCliente(cliente);
        agendamento.setVendedor(vendedor);
        agendamento.vincularEstabelecimento(servico.getEstabelecimento());
        agendamento.vincularProfissional(profissional);
        agendamento.setServico(servico);
        return AgendamentoResponse.from(agendamentos.save(agendamento));
    }

    public AgendamentoResponse cancelar(AgendamentoRequest request) {
        Agendamento agendamento = reservaAutorizada(request.getAgendamentoId(), true);
        if (agendamento.getStatus() == Status.CANCELADO) return AgendamentoResponse.from(agendamento);
        validarAlteracao(agendamento);
        agendamento.setStatus(Status.CANCELADO);
        return AgendamentoResponse.from(agendamentos.save(agendamento));
    }

    private Agendamento reservaAutorizada(Long id, boolean bloquear) {
        AgendamentoRequest request = new AgendamentoRequest();
        request.setAgendamentoId(id);
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
        if (bloquear) {
            agendamento = agendamentos.findParaAlteracao(id).orElseThrow(this::notFound);
            // Refresh after waiting for a concurrent cancellation/reschedule to commit.
            entityManager.refresh(agendamento);
        }
        return agendamento;
    }

    private final jakarta.persistence.EntityManager entityManager;

    private void validarAlteracao(Agendamento reserva) {
        if (reserva.getStatus() != Status.AGENDADO) throw new ResponseStatusException(HttpStatus.CONFLICT, "Agendamento cancelado não pode ser remarcado");
        var agora = java.time.ZonedDateTime.now(ZoneId.of(reserva.getEstabelecimento().getFusoHorario()));
        var limite = agora.toLocalDateTime().plusMinutes(reserva.getEstabelecimento().getAntecedenciaCancelamentoMinutos());
        if (!reserva.getData().atTime(reserva.getHoraInicio()).isAfter(limite))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este agendamento não pode mais ser cancelado ou remarcado dentro do prazo configurado");
    }

    private LocalTime duracaoContratada(Agendamento reserva) {
        return LocalTime.ofSecondOfDay(java.time.Duration.between(reserva.getHoraInicio(), reserva.getHoraFim()).getSeconds());
    }

    public AgendamentoResponse remarcar(Long id, LocalDate data, LocalTime hora) {
        if (data == null || hora == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a nova data e horário");
        Agendamento reserva = reservaAutorizada(id, true);
        validarAlteracao(reserva);
        validarMomentoFuturo(data, hora, reserva.getVendedor());
        Profissional profissional = profissionais.findAtivoDoEstabelecimentoParaReserva(
                reserva.getProfissional().getId(), reserva.getEstabelecimento().getId()).orElseThrow(this::notFound);
        LocalTime duracao = duracaoContratada(reserva);
        if (!disponibilidade.horarioDisponivel(profissional, duracao, data, hora, reserva.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário indisponível. Seu agendamento original foi mantido");
        reserva.setData(data);
        reserva.setHoraInicio(hora);
        reserva.setHoraFim(hora.plusSeconds(duracao.toSecondOfDay()));
        return AgendamentoResponse.from(agendamentos.save(reserva));
    }

    @Transactional(readOnly = true)
    public List<LocalTime> horariosRemarcacao(Long id, LocalDate data) {
        Agendamento reserva = reservaAutorizada(id, false);
        validarAlteracao(reserva);
        if (!reserva.getProfissional().isAtivo()) return List.of();
        LocalTime duracao = duracaoContratada(reserva);
        return disponibilidade.horariosRemarcacao(reserva.getProfissional(), duracao, data, id);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> meusAgendamentos() {
        Cliente cliente = currentUser.cliente();
        return agendamentos.findAllByClienteIdOrderByDataDescHoraInicioDesc(cliente.getId())
                .stream().map(AgendamentoResponse::from).toList();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }

    private void validarMomentoFuturo(LocalDate data, LocalTime horaInicio, Vendedor vendedor) {
        ZoneId fusoHorario = ZoneId.of(vendedor.getEstabelecimento().getFusoHorario());
        var agora = java.time.ZonedDateTime.now(fusoHorario);
        if (data.isBefore(agora.toLocalDate())
                || (data.isEqual(agora.toLocalDate()) && !horaInicio.isAfter(agora.toLocalTime()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível agendar em um horário que já passou");
        }
        var instante = data.atTime(horaInicio);
        var estabelecimento = vendedor.getEstabelecimento();
        if (instante.isBefore(agora.toLocalDateTime().plusMinutes(estabelecimento.getAntecedenciaMinimaMinutos()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este horário exige uma antecedência maior para agendamento");
        }
        if (data.isAfter(agora.toLocalDate().plusDays(estabelecimento.getJanelaMaximaAgendamentoDias()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este horário está fora da janela disponível para agendamento");
        }
    }
}
