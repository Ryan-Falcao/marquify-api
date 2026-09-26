package com.marquify.beta;

import com.marquify.beta.entity.*;
import com.marquify.beta.infra.security.TokenProperties;
import com.marquify.beta.infra.security.TokenService;
import com.marquify.beta.repository.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired TokenProperties tokenProperties;
    @Autowired PasswordEncoder encoder;
    @Autowired clienteRepository clientes;
    @Autowired vendedorRepository vendedores;
    @Autowired EstabelecimentoRepository estabelecimentos;
    @Autowired ProfissionalRepository profissionais;
    @Autowired servicoRepository servicos;
    @Autowired agendamentoRepository agendamentos;
    @Autowired DisponibilidadeProfissionalRepository disponibilidades;

    Cliente clienteA;
    Cliente clienteB;
    Vendedor vendedorA;
    Vendedor vendedorB;
    Servicos servicoA;
    Servicos servicoB;
    Agendamento reservaA;
    Agendamento reservaB;

    @BeforeEach
    void fixtures() {
        String hash = encoder.encode("test-password");
        clienteA = clientes.save(new Cliente("cliente-a@example.test", hash));
        clienteB = clientes.save(new Cliente("cliente-b@example.test", hash));
        vendedorA = vendedor("vendedor-a@example.test", hash);
        vendedorB = vendedor("vendedor-b@example.test", hash);
        servicoA = servico(vendedorA);
        servicoB = servico(vendedorB);
        reservaA = reserva(clienteA, vendedorA, servicoA);
        reservaB = reserva(clienteB, vendedorB, servicoB);
        agendamentos.flush();
    }

    @Test
    void requiresAuthenticationAndCorrectRole() throws Exception {
        mvc.perform(get("/vendedor/" + vendedorA.getId())).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        as(clienteA, get("/vendedor/" + vendedorA.getId())).andExpect(status().isForbidden());
    }

    @Test
    void frontendCanReadOnlyActivePublicCatalogWithCors() throws Exception {
        Servicos inativo = servico(vendedorA);
        inativo.desativar();
        servicos.saveAndFlush(inativo);
        Profissional profissionalInativo = new Profissional(vendedorA.getEstabelecimento(), "Indisponível");
        profissionalInativo.desativar();
        profissionais.saveAndFlush(profissionalInativo);
        String base = "/publico/estabelecimentos/" + vendedorA.getEstabelecimento().getId();

        mvc.perform(get(base)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendedorA.getEstabelecimento().getId()))
                .andExpect(jsonPath("$.nome").exists());
        mvc.perform(get(base + "/servicos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(servicoA.getId()))
                .andExpect(jsonPath("$[0].ativo").value(true));
        mvc.perform(get(base + "/profissionais")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(vendedorA.getProfissionalPrincipal().getId()))
                .andExpect(jsonPath("$[0].criadoEm").doesNotExist());
        mvc.perform(options(base + "/servicos")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void sellerCanReadOwnProfileButNotAnother() throws Exception {
        as(vendedorA, get("/vendedor/" + vendedorA.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendedorA.getId()))
                .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.authorities").doesNotExist());
        as(vendedorA, get("/vendedor/" + vendedorB.getId())).andExpect(status().isForbidden());
    }

    @Test
    void dashboardUsesOnlyTheSellerIdentifiedByJwt() throws Exception {
        as(vendedorA, get("/vendedor/me")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendedorA.getId()))
                .andExpect(jsonPath("$.estabelecimentoId").value(vendedorA.getEstabelecimento().getId()));
        as(vendedorA, get("/vendedor/me/servicos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(servicoA.getId()));
        as(vendedorA, get("/vendedor/me/profissionais")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(vendedorA.getProfissionalPrincipal().getId()));
        as(vendedorA, get("/vendedor/me/agendamentos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reservaA.getId()));
        as(clienteA, get("/vendedor/me")).andExpect(status().isForbidden());
    }

    @Test
    void rejectsAllCrossSellerProfileUpdates() throws Exception {
        String body = "{\"vendedor_id\":" + vendedorB.getId()
                + ",\"newNome\":\"Invadido\",\"newNomeLoja\":\"Invadida\",\"newDiasAbertos\":[\"Domingo\"]"
                + ",\"newHoraAbertura\":\"01:00:00\",\"newHoraFechamento\":\"02:00:00\"}";
        for (String action : new String[]{"mudarNome", "mudarNomeLoja", "mudarDiasAbertos",
                "mudarHoraAbertura", "mudarHoraFechamento"}) {
            as(vendedorA, put("/vendedor/" + action).content(body)).andExpect(status().isForbidden());
        }
        assertThat(vendedores.findById(vendedorB.getId()).orElseThrow().getNome()).isEqualTo("Proprietário");
        as(vendedorA, put("/vendedor/mudarNome").content("{\"vendedor_id\":" + vendedorA.getId()
                + ",\"newNome\":\"Novo nome\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo nome"));
    }

    @Test
    void sellerAgendaContainsOnlyOwnAppointmentsAndSafeSummaries() throws Exception {
        as(vendedorA, get("/vendedor/agendamentos").content("{\"vendedor_id\":" + vendedorA.getId() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reservaA.getId()))
                .andExpect(jsonPath("$[0].cliente.senha").doesNotExist())
                .andExpect(jsonPath("$[0].cliente.email").doesNotExist())
                .andExpect(jsonPath("$[0].vendedor.password").doesNotExist())
                .andExpect(jsonPath("$[0].servico.vendedor").doesNotExist())
                .andExpect(jsonPath("$[0].estabelecimento.id").value(vendedorA.getEstabelecimento().getId()))
                .andExpect(jsonPath("$[0].profissional.id").value(vendedorA.getProfissionalPrincipal().getId()));
        as(vendedorA, get("/vendedor/agendamentos").content("{\"vendedor_id\":" + vendedorB.getId() + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesBothSellerAndServiceOwnershipOnDeletion() throws Exception {
        as(vendedorA, delete("/vendedor/deletarServico").content(serviceBody(vendedorB, servicoB)))
                .andExpect(status().isForbidden());
        as(vendedorA, delete("/vendedor/deletarServico").content(serviceBody(vendedorA, servicoB)))
                .andExpect(status().isNotFound());
        assertThat(servicos.existsById(servicoB.getId())).isTrue();
        Servicos unused = servico(vendedorA);
        as(vendedorA, delete("/vendedor/deletarServico").content(serviceBody(vendedorA, unused)))
                .andExpect(status().isNoContent());
        assertThat(servicos.findById(unused.getId())).isPresent()
                .get().extracting(Servicos::isAtivo).isEqualTo(false);
    }

    @Test
    void ownerCanManageCatalogOnlyInOwnEstablishment() throws Exception {
        String ownBase = "/estabelecimentos/" + vendedorA.getEstabelecimento().getId() + "/servicos";
        String otherBase = "/estabelecimentos/" + vendedorB.getEstabelecimento().getId() + "/servicos";
        String body = "{\"nome\":\"Barba\",\"descricao\":\"Completa\",\"preco\":25,\"tempo\":\"00:25:00\",\"profissionaisIds\":["
                + vendedorA.getProfissionalPrincipal().getId() + "]}";

        as(vendedorA, get(ownBase)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ativo").value(true));
        as(vendedorA, get(otherBase)).andExpect(status().isForbidden());

        as(vendedorA, post(ownBase).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estabelecimentoId")
                        .value(vendedorA.getEstabelecimento().getId()))
                .andExpect(jsonPath("$.ativo").value(true));
        Long criadoId = servicos.findAllByEstabelecimentoIdOrderByNomeAsc(vendedorA.getEstabelecimento().getId()).stream()
                .filter(servico -> servico.getNome().equals("Barba"))
                .findFirst().orElseThrow().getId();

        String atualizado = "{\"nome\":\"Barba premium\",\"descricao\":\"Completa\",\"preco\":30,\"tempo\":\"00:30:00\",\"profissionaisIds\":["
                + vendedorA.getProfissionalPrincipal().getId() + "]}";
        as(vendedorA, put(ownBase + "/" + criadoId).content(atualizado))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Barba premium"));
        as(vendedorA, patch(ownBase + "/" + criadoId + "/desativar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        as(vendedorA, patch(ownBase + "/" + criadoId + "/ativar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
        as(vendedorB, put(ownBase + "/" + criadoId).content(atualizado)).andExpect(status().isForbidden());
    }

    @Test
    void serviceCreationRequiresOwnerAndReturnsDto() throws Exception {
        as(vendedorA, post("/vendedor/criarServico").content(serviceBody(vendedorB, servicoB)))
                .andExpect(status().isForbidden());
        as(vendedorA, post("/vendedor/criarServico").content("{\"vendedorId\":" + vendedorA.getId()
                + ",\"nome\":\"Corte\",\"preco\":30,\"tempo\":\"00:30:00\",\"profissionaisIds\":["
                + vendedorA.getProfissionalPrincipal().getId() + "]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.vendedorId").value(vendedorA.getId()))
                .andExpect(jsonPath("$.estabelecimentoId").value(vendedorA.getEstabelecimento().getId()))
                .andExpect(jsonPath("$.vendedor").doesNotExist())
                .andExpect(jsonPath("$.profissionais[0].id").value(vendedorA.getProfissionalPrincipal().getId()));
    }

    @Test
    void bookingUsesAuthenticatedClientAndRejectsSpoofedClient() throws Exception {
        long before = agendamentos.count();
        as(clienteA, post("/agendamento").content(booking(clienteB.getId(), vendedorA, servicoA)))
                .andExpect(status().isForbidden());
        assertThat(agendamentos.count()).isEqualTo(before);
        as(clienteA, post("/agendamento").content(booking(null, vendedorA, servicoA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cliente.id").value(clienteA.getId()))
                .andExpect(jsonPath("$.cliente.password").doesNotExist())
                .andExpect(jsonPath("$.vendedor.senha").doesNotExist())
                .andExpect(jsonPath("$.servico.vendedor").doesNotExist())
                .andExpect(jsonPath("$.estabelecimento.id").value(vendedorA.getEstabelecimento().getId()))
                .andExpect(jsonPath("$.profissional.id").value(vendedorA.getProfissionalPrincipal().getId()));
    }

    @Test
    void exposesAvailableSlotsAndRejectsOverlappingBooking() throws Exception {
        String gestao = "/vendedor/" + vendedorA.getId() + "/profissionais/"
                + vendedorA.getProfissionalPrincipal().getId() + "/disponibilidade";
        as(vendedorA, get(gestao)).andExpect(status().isOk())
                .andExpect(jsonPath("$.jornadas[0].diaSemana").value("MONDAY"));

        String horarios = "/publico/estabelecimentos/" + vendedorA.getEstabelecimento().getId()
                + "/profissionais/" + vendedorA.getProfissionalPrincipal().getId()
                + "/horarios?servicoId=" + servicoA.getId() + "&data=2027-01-04";
        mvc.perform(get(horarios)).andExpect(status().isOk())
                .andExpect(jsonPath("$.horarios[0]").value("08:00:00"));

        as(clienteA, post("/agendamento").content(booking(clienteA.getId(), vendedorA, servicoA)))
                .andExpect(status().isOk());
        as(clienteB, post("/agendamento").content(booking(clienteB.getId(), vendedorA, servicoA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Horário indisponível"));
    }

    @Test
    void bookingRejectsMismatchedSellerAndService() throws Exception {
        long before = agendamentos.count();
        as(clienteA, post("/agendamento").content(booking(clienteA.getId(), vendedorA, servicoB)))
                .andExpect(status().isNotFound());
        assertThat(agendamentos.count()).isEqualTo(before);
    }

    @Test
    void bookingRejectsProfessionalWhoDoesNotOfferTheService() throws Exception {
        Profissional outroProfissional = profissionais.saveAndFlush(new Profissional(
                vendedorA.getEstabelecimento(), "Outro profissional"));

        as(clienteA, post("/agendamento").content(booking(clienteA.getId(), vendedorA, servicoA, outroProfissional)))
                .andExpect(status().isConflict());
    }

    @Test
    void ownerCanManageOnlyProfessionalsOfOwnEstablishment() throws Exception {
        String ownBase = "/vendedor/" + vendedorA.getId() + "/profissionais";
        Profissional profissionalAlheio = profissionais.saveAndFlush(new Profissional(
                vendedorB.getEstabelecimento(), "Profissional alheio"));

        as(vendedorA, get(ownBase)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(vendedorA.getProfissionalPrincipal().getId()))
                .andExpect(jsonPath("$[0].estabelecimento").doesNotExist());
        as(vendedorA, post(ownBase).content("{\"nome\":\"Beatriz\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Beatriz"))
                .andExpect(jsonPath("$.ativo").value(true));

        Profissional beatriz = profissionais.findAllByEstabelecimentoIdOrderByNomeAsc(
                        vendedorA.getEstabelecimento().getId()).stream()
                .filter(profissional -> profissional.getNome().equals("Beatriz"))
                .findFirst().orElseThrow();
        as(vendedorA, put(ownBase + "/" + beatriz.getId()).content("{\"nome\":\"Beatriz Silva\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Beatriz Silva"));
        as(vendedorA, patch(ownBase + "/" + beatriz.getId() + "/desativar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        as(vendedorA, patch(ownBase + "/" + beatriz.getId() + "/ativar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));

        as(vendedorA, post("/vendedor/" + vendedorB.getId() + "/profissionais").content("{\"nome\":\"Invasão\"}"))
                .andExpect(status().isForbidden());
        as(vendedorA, put(ownBase + "/" + profissionalAlheio.getId()).content("{\"nome\":\"Invasão\"}"))
                .andExpect(status().isNotFound());
        as(clienteA, get(ownBase)).andExpect(status().isForbidden());
    }

    @Test
    void sellerCannotImpersonateClientWhenCreatingBooking() throws Exception {
        as(vendedorA, post("/agendamento").content(booking(clienteA.getId(), vendedorA, servicoA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCanCancelOnlyOwnBooking() throws Exception {
        as(clienteA, put("/agendamento/cancelar").content(cancel(reservaB))).andExpect(status().isNotFound());
        assertThat(agendamentos.findById(reservaB.getId()).orElseThrow().getStatus()).isEqualTo(Status.AGENDADO);
        as(clienteA, put("/agendamento/cancelar").content(cancel(reservaA))).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void sellerCanCancelOnlyOwnBusinessBooking() throws Exception {
        as(vendedorA, put("/agendamento/cancelar").content(cancel(reservaB))).andExpect(status().isNotFound());
        assertThat(agendamentos.findById(reservaB.getId()).orElseThrow().getStatus()).isEqualTo(Status.AGENDADO);
        as(vendedorA, put("/agendamento/cancelar").content(cancel(reservaA))).andExpect(status().isOk());
    }

    @Test
    void rejectsMalformedExpiredUnsignedLegacyAndDeletedAccountTokens() throws Exception {
        var key = Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        String expired = Jwts.builder().issuer("marquify-api").subject("cliente:" + clienteA.getId())
                .expiration(new Date(now.getTime() - 60_000)).signWith(key).compact();
        String legacy = Jwts.builder().issuer("marquify-api").subject(vendedorA.getEmail())
                .expiration(new Date(now.getTime() + 60_000)).signWith(key).compact();
        String unsigned = Jwts.builder().subject("vendedor:" + vendedorA.getId()).compact();
        String removed = Jwts.builder().issuer("marquify-api").subject("vendedor:9223372036854775807")
                .expiration(new Date(now.getTime() + 60_000)).signWith(key).compact();
        String wrongSignature = Jwts.builder().issuer("marquify-api").subject("vendedor:" + vendedorA.getId())
                .signWith(Keys.hmacShaKeyFor("different-test-only-key-of-at-least-32-bytes".getBytes(StandardCharsets.UTF_8))).compact();
        for (String token : new String[]{"invalid", "", expired, legacy, unsigned, removed, wrongSignature}) {
            mvc.perform(get("/vendedor/" + vendedorA.getId()).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        }
    }

    @Test
    void existingClientTokenCannotTurnIntoSellerWhenEmailCollides() throws Exception {
        String token = tokens.gerarToken(clienteA);
        vendedorA.setEmail(clienteA.getEmail());
        vendedores.saveAndFlush(vendedorA);
        mvc.perform(get("/vendedor/" + vendedorA.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"login\":\"" + clienteA.getEmail() + "\",\"senha\":\"test-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRejectsEmailsOfBothAccountTypesAndInvalidInput() throws Exception {
        for (String email : new String[]{clienteA.getEmail(), vendedorA.getEmail()}) {
            mvc.perform(post("/auth/register").contentType("application/json")
                    .content("{\"login\":\"" + email + "\",\"senha\":\"test-password\"}"))
                    .andExpect(status().isConflict());
        }
        mvc.perform(post("/auth/register").contentType("application/json").content("{\"login\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void commercialRegistrationCreatesOwnerBusinessProfessionalAndToken() throws Exception {
        String body = "{\"nome\":\"Joana\",\"estabelecimento\":\"Studio Joana\",\"email\":\"joana@example.test\",\"senha\":\"new-password\",\"fusoHorario\":\"America/Sao_Paulo\"}";
        mvc.perform(post("/auth/cadastro-comercial").contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.estabelecimentoId").isNumber())
                .andExpect(jsonPath("$.vendedorId").isNumber());
        Vendedor novo = vendedores.findAllByEmail("joana@example.test").getFirst();
        assertThat(novo.getEstabelecimento().getNome()).isEqualTo("Studio Joana");
        assertThat(novo.getProfissionalPrincipal()).isNotNull();
        assertThat(disponibilidades.findAllByProfissionalIdOrderByDiaSemanaAsc(novo.getProfissionalPrincipal().getId()))
                .hasSize(6);
    }

    @Test
    void loginWorksAndWrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"login\":\"" + clienteA.getEmail() + "\",\"senha\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"login\":\"" + clienteA.getEmail() + "\",\"senha\":\"test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
    }

    private ResultActions as(org.springframework.security.core.userdetails.UserDetails user,
                             MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + tokens.gerarToken(user))
                .contentType("application/json"));
    }

    private Vendedor vendedor(String email, String hash) {
        Vendedor v = new Vendedor();
        v.setNome("Proprietário");
        v.setEmail(email);
        v.vincularEstabelecimento(estabelecimentos.save(new Estabelecimento(email + " - estabelecimento", "America/Sao_Paulo")));
        v.setNomeLoja(email);
        v.setHoraAbertura(LocalTime.of(8, 0));
        v.setHoraFechamento(LocalTime.of(18, 0));
        v.setDiasAbertos(Set.of(DiasAbertos.Segunda));
        v.setSenha(hash);
        v.setRole(UserRole.ADMIN);
        Vendedor salvo = vendedores.save(v);
        Profissional principal = profissionais.save(new Profissional(salvo.getEstabelecimento(), "Profissional " + email));
        disponibilidades.save(new DisponibilidadeProfissional(principal, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(18, 0)));
        salvo.vincularProfissionalPrincipal(principal);
        return vendedores.save(salvo);
    }

    private Servicos servico(Vendedor vendedor) {
        Servicos s = new Servicos();
        s.setNome("Corte");
        s.setPreco(30.0);
        s.setTempo(LocalTime.of(0, 30));
        s.setEstabelecimento(vendedor.getEstabelecimento());
        s.setVendedor(vendedor);
        s.definirProfissionais(Set.of(vendedor.getProfissionalPrincipal()));
        return servicos.save(s);
    }

    private Agendamento reserva(Cliente cliente, Vendedor vendedor, Servicos servico) {
        Agendamento a = new Agendamento();
        a.setCliente(cliente);
        a.setVendedor(vendedor);
        a.vincularEstabelecimento(vendedor.getEstabelecimento());
        a.vincularProfissional(vendedor.getProfissionalPrincipal());
        a.setServico(servico);
        a.setData(LocalDate.now().plusDays(1));
        a.setHoraInicio(LocalTime.of(10, 0));
        a.setHoraFim(LocalTime.of(10, 30));
        a.setStatus(Status.AGENDADO);
        return agendamentos.save(a);
    }

    private String serviceBody(Vendedor vendedor, Servicos servico) {
        return "{\"vendedorId\":" + vendedor.getId() + ",\"servicoId\":" + servico.getId() + "}";
    }

    private String cancel(Agendamento agendamento) {
        return "{\"agendamentoId\":" + agendamento.getId() + "}";
    }

    private String booking(Long clienteId, Vendedor vendedor, Servicos servico) {
        return booking(clienteId, vendedor, servico, vendedor.getProfissionalPrincipal());
    }

    private String booking(Long clienteId, Vendedor vendedor, Servicos servico, Profissional profissional) {
        return "{\"clienteId\":" + clienteId + ",\"vendedorId\":" + vendedor.getId()
                + ",\"servicoId\":" + servico.getId() + ",\"profissionalId\":" + profissional.getId()
                + ",\"data\":\"2027-01-04\",\"horaInicio\":\"10:00:00\"}";
    }
}
