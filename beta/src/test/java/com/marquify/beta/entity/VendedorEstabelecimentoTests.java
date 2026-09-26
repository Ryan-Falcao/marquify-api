package com.marquify.beta.entity;

import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.vendedorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VendedorEstabelecimentoTests {

    @Autowired
    EstabelecimentoRepository estabelecimentos;

    @Autowired
    vendedorRepository vendedores;

    @Autowired
    ProfissionalRepository profissionais;

    @Test
    void vinculaContaDoProprietarioAoEstabelecimento() {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Barbearia Central", "America/Sao_Paulo"));
        Vendedor vendedor = vendedor();
        vendedor.vincularEstabelecimento(estabelecimento);

        Vendedor salvo = vendedores.saveAndFlush(vendedor);

        assertThat(salvo.getEstabelecimento().getId()).isEqualTo(estabelecimento.getId());
        assertThat(vendedores.existsByEstabelecimentoId(estabelecimento.getId())).isTrue();
    }

    @Test
    void profissionalPrincipalDoProprietarioDeveSerDoMesmoEstabelecimento() {
        Estabelecimento estabelecimento = estabelecimentos.saveAndFlush(
                new Estabelecimento("Barbearia Central", "America/Sao_Paulo"));
        Vendedor vendedor = vendedor();
        vendedor.vincularEstabelecimento(estabelecimento);
        Profissional profissional = profissionais.saveAndFlush(new Profissional(estabelecimento, "Ana Proprietária"));

        vendedor.vincularProfissionalPrincipal(profissional);
        Vendedor salvo = vendedores.saveAndFlush(vendedor);

        assertThat(salvo.getProfissionalPrincipal().getId()).isEqualTo(profissional.getId());
    }

    private Vendedor vendedor() {
        Vendedor vendedor = new Vendedor();
        vendedor.setNome("Ana Proprietária");
        vendedor.setEmail("ana@example.test");
        vendedor.setNomeLoja("Barbearia Central");
        vendedor.setHoraAbertura(LocalTime.of(8, 0));
        vendedor.setHoraFechamento(LocalTime.of(18, 0));
        vendedor.setDiasAbertos(Set.of(DiasAbertos.Segunda));
        vendedor.setSenha("hash-de-teste");
        vendedor.setRole(UserRole.ADMIN);
        return vendedor;
    }
}
