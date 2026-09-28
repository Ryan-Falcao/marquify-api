package com.marquify.beta.service;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.DisponibilidadeProfissional;
import com.marquify.beta.entity.Profissional;
import com.marquify.beta.entity.Servicos;
import com.marquify.beta.entity.Status;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.DisponibilidadeProfissionalRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.agendamentoRepository;
import com.marquify.beta.repository.servicoRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.repository.BloqueioAgendaRepository;
import com.marquify.beta.request.DisponibilidadeRequest;
import com.marquify.beta.response.DisponibilidadeResponse;
import com.marquify.beta.response.HorariosLivresResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class DisponibilidadeService {
    private static final int INTERVALO_MINUTOS = 15;

    private final DisponibilidadeProfissionalRepository disponibilidades;
    private final ProfissionalRepository profissionais;
    private final servicoRepository servicos;
    private final agendamentoRepository agendamentos;
    private final vendedorRepository vendedores;
    private final CurrentUser currentUser;
    private final BloqueioAgendaRepository bloqueios;

    @Transactional(readOnly = true)
    public DisponibilidadeResponse consultarGestao(Long vendedorId, Long profissionalId) {
        profissionalDoVendedor(vendedorId, profissionalId);
        return respostaDisponibilidade(profissionalId);
    }

    public DisponibilidadeResponse substituir(Long vendedorId, Long profissionalId, DisponibilidadeRequest request) {
        Profissional profissional = profissionalDoVendedor(vendedorId, profissionalId);
        HashSet<java.time.DayOfWeek> dias = new HashSet<>();
        for (DisponibilidadeRequest.Jornada jornada : request.jornadas()) {
            if (!jornada.horaInicio().isBefore(jornada.horaFim()) || !dias.add(jornada.diaSemana())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jornadas inválidas ou repetidas");
            }
        }
        disponibilidades.deleteByProfissionalId(profissional.getId());
        // A jornada possui unicidade por profissional e dia. O delete precisa chegar ao banco
        // antes dos inserts para que a substituição de um dia já existente não viole a constraint.
        disponibilidades.flush();
        disponibilidades.saveAll(request.jornadas().stream().map(jornada ->
                new DisponibilidadeProfissional(profissional, jornada.diaSemana(), jornada.horaInicio(), jornada.horaFim())
        ).toList());
        return respostaDisponibilidade(profissionalId);
    }

    @Transactional(readOnly = true)
    public DisponibilidadeResponse consultarGestaoAtual(Long profissionalId) {
        Profissional profissional = profissionalDoVendedorAtual(profissionalId);
        return respostaDisponibilidade(profissional.getId());
    }

    public DisponibilidadeResponse substituirAtual(Long profissionalId, DisponibilidadeRequest request) {
        Profissional profissional = profissionalDoVendedorAtual(profissionalId);
        HashSet<java.time.DayOfWeek> dias = new HashSet<>();
        for (DisponibilidadeRequest.Jornada jornada : request.jornadas()) {
            if (!jornada.horaInicio().isBefore(jornada.horaFim()) || !dias.add(jornada.diaSemana())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jornadas inválidas ou repetidas");
            }
        }
        disponibilidades.deleteByProfissionalId(profissional.getId());
        disponibilidades.flush();
        disponibilidades.saveAll(request.jornadas().stream().map(jornada ->
                new DisponibilidadeProfissional(profissional, jornada.diaSemana(), jornada.horaInicio(), jornada.horaFim())
        ).toList());
        return respostaDisponibilidade(profissional.getId());
    }

    @Transactional(readOnly = true)
    public HorariosLivresResponse horariosLivres(Long estabelecimentoId, Long profissionalId, Long servicoId, LocalDate data) {
        if (data == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data é obrigatória");
        Servicos servico = servicos.findByIdAndEstabelecimentoIdAndAtivoTrue(servicoId, estabelecimentoId).orElseThrow(this::notFound);
        Profissional profissional = profissionais.findByIdAndEstabelecimentoIdAndAtivoTrue(profissionalId, estabelecimentoId)
                .orElseThrow(this::notFound);
        if (!servico.executadoPor(profissionalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profissional não oferece o serviço selecionado");
        }
        List<LocalTime> candidatos = horariosPossiveis(profissional, servico, data).stream()
                .filter(horario -> momentoPermitido(profissional, data, horario))
                .toList();
        List<Agendamento> reservas = agendamentos.findAllByProfissionalIdAndDataAndStatus(
                profissional.getId(), data, Status.AGENDADO);
        var bloqueiosDaData = bloqueios.buscarNoPeriodo(profissional.getId(), data, data);
        List<LocalTime> bloqueados = candidatos.stream().filter(horario -> {
            LocalTime fim = horario.plusHours(servico.getTempo().getHour()).plusMinutes(servico.getTempo().getMinute());
            return bloqueiosDaData.stream().anyMatch(bloqueio -> bloqueio.bloqueia(data, horario, fim));
        }).toList();
        List<LocalTime> ocupados = candidatos.stream().filter(horario -> {
            LocalTime fim = horario.plusHours(servico.getTempo().getHour()).plusMinutes(servico.getTempo().getMinute());
            return reservas.stream().anyMatch(reserva -> sobrepoeComIntervalo(profissional, horario, fim, reserva));
        }).filter(horario -> !bloqueados.contains(horario)).toList();
        List<LocalTime> livres = candidatos.stream()
                .filter(horario -> !ocupados.contains(horario) && !bloqueados.contains(horario)).toList();
        return new HorariosLivresResponse(estabelecimentoId, profissionalId, servicoId, data,
                livres, ocupados, bloqueados);
    }

    public boolean horarioDisponivel(Profissional profissional, Servicos servico, LocalDate data, LocalTime horaInicio) {
        return horarioDisponivel(profissional, servico.getTempo(), data, horaInicio, null);
    }

    public List<LocalTime> horariosRemarcacao(Profissional profissional, LocalTime duracao, LocalDate data, Long ignorarId) {
        var jornadas = disponibilidades.findAllByProfissionalIdAndDiaSemana(profissional.getId(), data.getDayOfWeek());
        var reservas = agendamentos.findAllByProfissionalIdAndDataAndStatus(profissional.getId(), data, Status.AGENDADO);
        var pausas = bloqueios.buscarNoPeriodo(profissional.getId(), data, data);
        return java.util.stream.IntStream.range(0, 96).mapToObj(i -> LocalTime.ofSecondOfDay(i * 900))
                .filter(inicio -> {
                    LocalTime fim = inicio.plusSeconds(duracao.toSecondOfDay());
                    return fim.isAfter(inicio) && momentoPermitido(profissional, data, inicio)
                            && jornadas.stream().anyMatch(j -> !inicio.isBefore(j.getHoraInicio()) && !fim.isAfter(j.getHoraFim()))
                            && pausas.stream().noneMatch(p -> p.bloqueia(data, inicio, fim))
                            && reservas.stream().filter(r -> !r.getId().equals(ignorarId))
                                .noneMatch(r -> sobrepoeComIntervalo(profissional, inicio, fim, r));
                }).toList();
    }

    public boolean horarioDisponivel(Profissional profissional, LocalTime duracao, LocalDate data, LocalTime horaInicio, Long ignorarId) {
        if (data == null || horaInicio == null || duracao == null || duracao.equals(LocalTime.MIDNIGHT)) return false;
        if (!momentoPermitido(profissional, data, horaInicio)) return false;
        LocalTime horaFim = horaInicio.plusSeconds(duracao.toSecondOfDay());
        if (!horaFim.isAfter(horaInicio)) return false;
        boolean dentroDaJornada = disponibilidades.findAllByProfissionalIdAndDiaSemana(profissional.getId(), data.getDayOfWeek())
                .stream().anyMatch(jornada -> !horaInicio.isBefore(jornada.getHoraInicio()) && !horaFim.isAfter(jornada.getHoraFim()));
        if (!dentroDaJornada) return false;
        boolean bloqueado = bloqueios.buscarNoPeriodo(profissional.getId(), data, data)
                .stream().anyMatch(bloqueio -> bloqueio.bloqueia(data, horaInicio, horaFim));
        if (bloqueado) return false;
        return agendamentos.findAllByProfissionalIdAndDataAndStatus(profissional.getId(), data, Status.AGENDADO).stream()
                .filter(agendamento -> !agendamento.getId().equals(ignorarId))
                .noneMatch(agendamento -> sobrepoeComIntervalo(profissional, horaInicio, horaFim, agendamento));
    }

    private List<LocalTime> horariosLivres(Profissional profissional, Servicos servico, LocalDate data) {
        return horariosPossiveis(profissional, servico, data).stream()
                .filter(horario -> horarioDisponivel(profissional, servico, data, horario))
                .toList();
    }

    private List<LocalTime> horariosPossiveis(Profissional profissional, Servicos servico, LocalDate data) {
        return disponibilidades.findAllByProfissionalIdAndDiaSemana(profissional.getId(), data.getDayOfWeek()).stream()
                .flatMap(jornada -> {
                    LocalTime ultimoInicio = jornada.getHoraFim().minusHours(servico.getTempo().getHour())
                            .minusMinutes(servico.getTempo().getMinute());
                    return java.util.stream.Stream.iterate(jornada.getHoraInicio(), horario -> !horario.isAfter(ultimoInicio),
                            horario -> horario.plusMinutes(INTERVALO_MINUTOS));
                })
                .toList();
    }

    private boolean momentoPermitido(Profissional profissional, LocalDate data, LocalTime horaInicio) {
        ZoneId fusoHorario = ZoneId.of(profissional.getEstabelecimento().getFusoHorario());
        ZonedDateTime agora = ZonedDateTime.now(fusoHorario);
        var estabelecimento = profissional.getEstabelecimento();
        if (data.isAfter(agora.toLocalDate().plusDays(estabelecimento.getJanelaMaximaAgendamentoDias()))) return false;
        return data.atTime(horaInicio).isAfter(agora.toLocalDateTime().plusMinutes(estabelecimento.getAntecedenciaMinimaMinutos()));
    }

    private DisponibilidadeResponse respostaDisponibilidade(Long profissionalId) {
        return DisponibilidadeResponse.from(profissionalId,
                disponibilidades.findAllByProfissionalIdOrderByDiaSemanaAsc(profissionalId));
    }

    private Profissional profissionalDoVendedor(Long vendedorId, Long profissionalId) {
        currentUser.vendedor(vendedorId);
        Vendedor vendedor = vendedores.findById(vendedorId).orElseThrow(this::notFound);
        return profissionais.findByIdAndEstabelecimentoId(profissionalId, vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
    }

    private Profissional profissionalDoVendedorAtual(Long profissionalId) {
        Vendedor atual = currentUser.vendedor();
        Vendedor vendedor = vendedores.findById(atual.getId()).orElseThrow(this::notFound);
        return profissionais.findByIdAndEstabelecimentoId(profissionalId, vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
    }

    private boolean sobrepoe(LocalTime inicio, LocalTime fim, LocalTime outroInicio, LocalTime outroFim) {
        return inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
    }

    @Transactional(readOnly = true)
    public DisponibilidadeResponse consultarDoProfissional() {
        return respostaDisponibilidade(currentUser.profissional().getId());
    }

    public DisponibilidadeResponse substituirDoProfissional(DisponibilidadeRequest request) {
        Profissional profissional = currentUser.profissional();
        HashSet<java.time.DayOfWeek> dias = new HashSet<>();
        for (DisponibilidadeRequest.Jornada jornada : request.jornadas()) {
            if (!jornada.horaInicio().isBefore(jornada.horaFim()) || !dias.add(jornada.diaSemana())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jornadas inválidas ou repetidas");
        }
        disponibilidades.deleteByProfissionalId(profissional.getId()); disponibilidades.flush();
        disponibilidades.saveAll(request.jornadas().stream().map(j -> new DisponibilidadeProfissional(profissional,j.diaSemana(),j.horaInicio(),j.horaFim())).toList());
        return respostaDisponibilidade(profissional.getId());
    }

    private boolean sobrepoeComIntervalo(Profissional profissional, LocalTime inicio, LocalTime fim, Agendamento reserva) {
        int intervalo = profissional.getEstabelecimento().getIntervaloEntreServicosMinutos();
        if (intervalo == 0) return sobrepoe(inicio, fim, reserva.getHoraInicio(), reserva.getHoraFim());
        return inicio.isBefore(reserva.getHoraFim().plusMinutes(intervalo))
                && reserva.getHoraInicio().minusMinutes(intervalo).isBefore(fim);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }
}
