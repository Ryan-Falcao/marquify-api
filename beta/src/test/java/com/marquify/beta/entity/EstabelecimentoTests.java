package com.marquify.beta.entity;

import com.marquify.beta.repository.EstabelecimentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EstabelecimentoTests {

    @Autowired
    EstabelecimentoRepository estabelecimentos;

    @Test
    void persisteDadosFundamentaisDoEstabelecimento() {
        Estabelecimento salvo = estabelecimentos.saveAndFlush(
                new Estabelecimento("Barbearia Central", "America/Sao_Paulo"));

        Estabelecimento encontrado = estabelecimentos.findById(salvo.getId()).orElseThrow();

        assertThat(encontrado.getNome()).isEqualTo("Barbearia Central");
        assertThat(encontrado.getFusoHorario()).isEqualTo("America/Sao_Paulo");
        assertThat(encontrado.isAtivo()).isTrue();
        assertThat(encontrado.getCriadoEm()).isNotNull();
        assertThat(encontrado.getAtualizadoEm()).isNotNull();
    }

    @Test
    void validaNomeEFusoHorario() {
        assertThatThrownBy(() -> new Estabelecimento(" ", "America/Sao_Paulo"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Estabelecimento("Clínica", "Fuso/Inexistente"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void atualizaAuditoriaAoAlterarEntidade() throws InterruptedException {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Clínica Vida", "America/Sao_Paulo"));
        Instant criadoEm = estabelecimento.getCriadoEm();
        Instant primeiraAtualizacao = estabelecimento.getAtualizadoEm();

        Thread.sleep(2);
        estabelecimento.alterarNome("Clínica Vida Nova");
        estabelecimentos.saveAndFlush(estabelecimento);

        assertThat(estabelecimento.getCriadoEm()).isEqualTo(criadoEm);
        assertThat(estabelecimento.getAtualizadoEm()).isAfter(primeiraAtualizacao);
    }
}
