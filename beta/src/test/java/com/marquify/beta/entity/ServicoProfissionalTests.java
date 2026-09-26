package com.marquify.beta.entity;

import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.servicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServicoProfissionalTests {

    @Autowired EstabelecimentoRepository estabelecimentos;
    @Autowired ProfissionalRepository profissionais;
    @Autowired servicoRepository servicos;

    @Test
    void associaUmServicoAProfissionaisDoMesmoEstabelecimento() {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Studio Central", "America/Sao_Paulo"));
        Profissional ana = profissionais.saveAndFlush(new Profissional(estabelecimento, "Ana"));
        Profissional bruno = profissionais.saveAndFlush(new Profissional(estabelecimento, "Bruno"));
        Servicos servico = new Servicos();
        servico.setNome("Corte");
        servico.setEstabelecimento(estabelecimento);
        servico.definirProfissionais(Set.of(ana, bruno));

        Servicos salvo = servicos.saveAndFlush(servico);

        assertThat(salvo.executadoPor(ana.getId())).isTrue();
        assertThat(salvo.executadoPor(bruno.getId())).isTrue();
        assertThat(servicos.findByIdAndEstabelecimentoId(salvo.getId(), estabelecimento.getId())).contains(salvo);
    }

    @Test
    void exigeAoMenosUmProfissional() {
        Servicos servico = new Servicos();
        assertThatThrownBy(() -> servico.definirProfissionais(Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
