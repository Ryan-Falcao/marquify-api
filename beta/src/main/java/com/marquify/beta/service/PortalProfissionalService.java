package com.marquify.beta.service;
import com.marquify.beta.entity.*;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.*;
import com.marquify.beta.request.StatusAtendimentoRequest;
import com.marquify.beta.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional public class PortalProfissionalService {
 private final CurrentUser currentUser; private final agendamentoRepository agendamentos;
 @Transactional(readOnly = true) public ProfissionalResponse perfil() { return ProfissionalResponse.from(currentUser.profissional()); }
 @Transactional(readOnly = true) public List<AgendamentoResponse> agenda(LocalDate data) { Profissional profissional=currentUser.profissional(); LocalDate dia=data==null?ZonedDateTime.now(ZoneId.of(profissional.getEstabelecimento().getFusoHorario())).toLocalDate():data; return agendamentos.findAllByProfissionalIdAndDataOrderByHoraInicioAsc(profissional.getId(),dia).stream().map(AgendamentoResponse::from).toList(); }
 public AgendamentoResponse alterarStatus(Long id, StatusAtendimentoRequest request) { Profissional profissional=currentUser.profissional(); if (request.status()!=Status.EM_ATENDIMENTO && request.status()!=Status.CONCLUIDO) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Status de atendimento inválido"); Agendamento agenda=agendamentos.findByIdAndProfissionalId(id,profissional.getId()).orElseThrow(this::notFound); if (agenda.getStatus()==Status.CANCELADO) throw new ResponseStatusException(HttpStatus.CONFLICT,"Agendamento cancelado não pode ser alterado"); agenda.setStatus(request.status()); return AgendamentoResponse.from(agendamentos.save(agenda)); }
 private ResponseStatusException notFound(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Recurso não encontrado");}
}
