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
        disponibilidades.saveAll(request.jornadas().stream().map(jornada ->
                new DisponibilidadeProfissional(profissional, jornada.diaSemana(), jornada.horaInicio(), jornada.horaFim())
        ).toList());
        return respostaDisponibilidade(profissionalId);
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
        return new HorariosLivresResponse(estabelecimentoId, profissionalId, servicoId, data,
                horariosLivres(profissional, servico, data));
    }

    public boolean horarioDisponivel(Profissional profissional, Servicos servico, LocalDate data, LocalTime horaInicio) {
        if (data == null || horaInicio == null || servico.getTempo() == null) return false;
        LocalTime horaFim = horaInicio.plusHours(servico.getTempo().getHour()).plusMinutes(servico.getTempo().getMinute());
        boolean dentroDaJornada = disponibilidades.findAllByProfissionalIdAndDiaSemana(profissional.getId(), data.getDayOfWeek())
                .stream().anyMatch(jornada -> !horaInicio.isBefore(jornada.getHoraInicio()) && !horaFim.isAfter(jornada.getHoraFim()));
        if (!dentroDaJornada) return false;
        return agendamentos.findAllByProfissionalIdAndDataAndStatus(profissional.getId(), data, Status.AGENDADO).stream()
                .noneMatch(agendamento -> sobrepoe(horaInicio, horaFim, agendamento.getHoraInicio(), agendamento.getHoraFim()));
    }

    private List<LocalTime> horariosLivres(Profissional profissional, Servicos servico, LocalDate data) {
        return disponibilidades.findAllByProfissionalIdAndDiaSemana(profissional.getId(), data.getDayOfWeek()).stream()
                .flatMap(jornada -> {
                    LocalTime ultimoInicio = jornada.getHoraFim().minusHours(servico.getTempo().getHour())
                            .minusMinutes(servico.getTempo().getMinute());
                    return java.util.stream.Stream.iterate(jornada.getHoraInicio(), horario -> !horario.isAfter(ultimoInicio),
                            horario -> horario.plusMinutes(INTERVALO_MINUTOS));
                })
                .filter(horario -> horarioDisponivel(profissional, servico, data, horario))
                .toList();
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

    private boolean sobrepoe(LocalTime inicio, LocalTime fim, LocalTime outroInicio, LocalTime outroFim) {
        return inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }
}
