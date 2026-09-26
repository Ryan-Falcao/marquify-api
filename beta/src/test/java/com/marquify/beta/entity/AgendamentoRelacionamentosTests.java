package com.marquify.beta.entity;

import com.marquify.beta.repository.agendamentoRepository;
import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoRelacionamentosTests {

    @Autowired EstabelecimentoRepository estabelecimentos;
    @Autowired ProfissionalRepository profissionais;
    @Autowired agendamentoRepository agendamentos;

    @Test
    void persisteEstabelecimentoEProfissionalDoAgendamento() {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Studio Central", "America/Sao_Paulo"));
        Profissional profissional = profissionais.saveAndFlush(new Profissional(estabelecimento, "Marina"));
        Agendamento agendamento = new Agendamento();
        agendamento.setData(LocalDate.of(2027, 1, 4));
        agendamento.setHoraInicio(LocalTime.of(10, 0));
        agendamento.setHoraFim(LocalTime.of(10, 30));
        agendamento.setStatus(Status.AGENDADO);
        agendamento.vincularEstabelecimento(estabelecimento);
        agendamento.vincularProfissional(profissional);

        Agendamento salvo = agendamentos.saveAndFlush(agendamento);

        assertThat(salvo.getEstabelecimento().getId()).isEqualTo(estabelecimento.getId());
        assertThat(salvo.getProfissional().getId()).isEqualTo(profissional.getId());
    }
}
