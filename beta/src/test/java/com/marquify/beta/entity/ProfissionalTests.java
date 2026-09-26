package com.marquify.beta.entity;

import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfissionalTests {

    @Autowired
    EstabelecimentoRepository estabelecimentos;

    @Autowired
    ProfissionalRepository profissionais;

    @Test
    void permiteVariosProfissionaisAtivosNoMesmoEstabelecimento() {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Clínica Bem Estar", "America/Sao_Paulo"));

        Profissional ana = profissionais.saveAndFlush(new Profissional(estabelecimento, "Ana"));
        Profissional bruno = profissionais.saveAndFlush(new Profissional(estabelecimento, "Bruno"));

        assertThat(profissionais.findAllByEstabelecimentoIdAndAtivoTrue(estabelecimento.getId()))
                .extracting(Profissional::getId)
                .containsExactlyInAnyOrder(ana.getId(), bruno.getId());
        assertThat(ana.getCriadoEm()).isNotNull();
        assertThat(bruno.getEstabelecimento().getId()).isEqualTo(estabelecimento.getId());
    }

    @Test
    void profissionalInativoSaiDaListaDeProfissionaisDisponiveis() {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Barbearia Norte", "America/Sao_Paulo"));
        Profissional profissional = profissionais.saveAndFlush(new Profissional(estabelecimento, "Carlos"));

        profissional.desativar();
        profissionais.saveAndFlush(profissional);

        assertThat(profissionais.findAllByEstabelecimentoIdAndAtivoTrue(estabelecimento.getId())).isEmpty();
    }

    @Test
    void validaDadosFundamentaisDoProfissional() {
        Estabelecimento estabelecimento = new Estabelecimento("Studio", "America/Sao_Paulo");
        assertThatThrownBy(() -> new Profissional(null, "Ana")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profissional(estabelecimento, " ")).isInstanceOf(IllegalArgumentException.class);
    }
}
