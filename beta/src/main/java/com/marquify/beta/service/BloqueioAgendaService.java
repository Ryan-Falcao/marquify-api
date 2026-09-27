package com.marquify.beta.service;

import com.marquify.beta.entity.BloqueioAgenda;
import com.marquify.beta.entity.Profissional;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.BloqueioAgendaRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.request.BloqueioAgendaRequest;
import com.marquify.beta.response.BloqueioAgendaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BloqueioAgendaService {
    private final BloqueioAgendaRepository bloqueios;
    private final ProfissionalRepository profissionais;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<BloqueioAgendaResponse> listarAtual(Long profissionalId, LocalDate inicio, LocalDate fim) {
        Profissional profissional = profissionalAtual(profissionalId);
        LocalDate de = inicio == null ? LocalDate.now() : inicio;
        LocalDate ate = fim == null ? de.plusYears(1) : fim;
        if (ate.isBefore(de)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido");
        return bloqueios.buscarNoPeriodo(profissional.getId(), de, ate)
                .stream().map(BloqueioAgendaResponse::from).toList();
    }

    public BloqueioAgendaResponse criarAtual(Long profissionalId, BloqueioAgendaRequest request) {
        Profissional profissional = profissionalAtual(profissionalId);
        if (request.dataFim() != null && request.dataFim().isBefore(request.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data final deve ser igual ou posterior à inicial");
        }
        try {
            return BloqueioAgendaResponse.from(bloqueios.save(new BloqueioAgenda(profissional, request.tipo(),
                    request.dataInicio(), request.dataFim(), request.horaInicio(), request.horaFim(), request.motivo(),
                    request.recorrente())));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    public void excluirAtual(Long profissionalId, Long bloqueioId) {
        Profissional profissional = profissionalAtual(profissionalId);
        BloqueioAgenda bloqueio = bloqueios.findByIdAndProfissionalId(bloqueioId, profissional.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado"));
        bloqueios.delete(bloqueio);
    }

    private Profissional profissionalAtual(Long profissionalId) {
        var vendedor = currentUser.vendedor();
        return profissionais.findByIdAndEstabelecimentoId(profissionalId, vendedor.getEstabelecimento().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado"));
    }
}
