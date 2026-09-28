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
import org.springframework.mock.web.MockMultipartFile;
import com.jayway.jsonpath.JsonPath;
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
    @org.springframework.test.context.bean.override.mockito.MockitoBean java.time.Clock dashboardClock;
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
    @Autowired AssinaturaRepository assinaturas;

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
        org.mockito.Mockito.when(dashboardClock.withZone(org.mockito.ArgumentMatchers.any(java.time.ZoneId.class)))
                .thenAnswer(invocation -> java.time.Clock.fixed(java.time.Instant.parse("2027-01-04T13:00:00Z"), invocation.getArgument(0)));
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
    void dashboardFiltersPaginatesAndKeepsOtherBusinessesPrivate() throws Exception {
        reservaA.setData(LocalDate.of(2027,1,4));
        reservaA.setValorCobrado(new java.math.BigDecimal("30.00"));
        var second = reserva(clienteA,vendedorA,servicoA); second.setData(reservaA.getData()); second.setHoraInicio(LocalTime.of(11,0)); second.setHoraFim(LocalTime.of(11,30));
        agendamentos.flush();
        String endpoint = "/vendedor/me/dashboard/agendamentos";
        as(vendedorA,get(endpoint).param("tamanho","1").param("direcao","ASC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2)).andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].id").value(reservaA.getId()));
        as(vendedorA,get(endpoint).param("tamanho","1").param("pagina","1").param("direcao","ASC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].id").value(second.getId()));
        as(vendedorA,get(endpoint).param("clienteId",clienteB.getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(0));
        as(vendedorA,get(endpoint).param("profissionalId",vendedorB.getProfissionalPrincipal().getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(0));
        as(vendedorA,get(endpoint).param("status","PREVISTO").param("clienteId",clienteA.getId().toString())
                .param("profissionalId",vendedorA.getProfissionalPrincipal().getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(1));
        mvc.perform(get(endpoint)).andExpect(status().isUnauthorized());
        as(clienteA,get(endpoint)).andExpect(status().isForbidden());
        as(clienteA,get("/vendedor/me/dashboard/indicadores")).andExpect(status().isForbidden());
    }

    @Test
    void dashboardAggregatesFullPeriodUsingContractPriceAndDerivedStatus() throws Exception {
        LocalDate date = LocalDate.of(2027,1,4);
        reservaA.setData(date); reservaA.setHoraInicio(LocalTime.of(8,0)); reservaA.setHoraFim(LocalTime.of(8,30)); reservaA.setValorCobrado(new java.math.BigDecimal("30"));
        var current = reserva(clienteA,vendedorA,servicoA); current.setData(date); current.setHoraInicio(LocalTime.of(9,45)); current.setHoraFim(LocalTime.of(10,15)); current.setValorCobrado(new java.math.BigDecimal("40"));
        var future = reserva(clienteA,vendedorA,servicoA); future.setData(date); future.setHoraInicio(LocalTime.of(11,0)); future.setHoraFim(LocalTime.of(11,30)); future.setValorCobrado(new java.math.BigDecimal("50"));
        var canceled = reserva(clienteA,vendedorA,servicoA); canceled.setData(date); canceled.setStatus(Status.CANCELADO); canceled.setValorCobrado(new java.math.BigDecimal("100"));
        servicoA.setPreco(999.0); reservaB.setData(date); reservaB.setValorCobrado(new java.math.BigDecimal("10000"));
        agendamentos.flush();
        String endpoint = "/vendedor/me/dashboard/indicadores";
        as(vendedorA,get(endpoint).param("tamanho","1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalAgendamentos").value(4))
                .andExpect(jsonPath("$.previstos").value(1)).andExpect(jsonPath("$.emAtendimento").value(1))
                .andExpect(jsonPath("$.finalizados").value(1)).andExpect(jsonPath("$.cancelamentos").value(1))
                .andExpect(jsonPath("$.faturamentoRealizado").value(30)).andExpect(jsonPath("$.faturamentoPrevisto").value(90));
        as(vendedorA,get(endpoint).param("status","CANCELADO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalAgendamentos").value(1))
                .andExpect(jsonPath("$.faturamentoPrevisto").value(0));
        as(vendedorA,get(endpoint).param("inicio","2027-01-05").param("fim","2027-01-06"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalAgendamentos").value(0))
                .andExpect(jsonPath("$.finalizados").value(0)).andExpect(jsonPath("$.faturamentoRealizado").value(0));
    }

    @Test
    void dashboardUsesBusinessDateAcrossMidnight() throws Exception {
        org.mockito.Mockito.when(dashboardClock.withZone(org.mockito.ArgumentMatchers.any(java.time.ZoneId.class)))
                .thenAnswer(invocation -> java.time.Clock.fixed(java.time.Instant.parse("2027-01-05T01:00:00Z"), invocation.getArgument(0)));
        reservaA.setData(LocalDate.of(2027,1,4)); reservaA.setHoraInicio(LocalTime.of(22,0)); reservaA.setHoraFim(LocalTime.of(22,30));
        agendamentos.flush();
        as(vendedorA,get("/vendedor/me/dashboard/agendamentos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.periodo.inicio").value("2027-01-04"))
                .andExpect(jsonPath("$.periodo.fusoHorario").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.itens[0].status").value("EM_ATENDIMENTO"));
    }

    @Test
    void dashboardRejectsInvalidFilters() throws Exception {
        String path = "/vendedor/me/dashboard/agendamentos";
        as(vendedorA,get(path).param("inicio","2027-01-04")).andExpect(status().isBadRequest());
        as(vendedorA,get(path).param("inicio","2027-01-05").param("fim","2027-01-04")).andExpect(status().isBadRequest());
        for (String[] invalid : new String[][]{{"pagina","-1"},{"tamanho","101"},{"status","FAKE"},{"ordenarPor","senha"},{"direcao","FAKE"},{"clienteId","0"},{"inicio","not-a-date"}})
            as(vendedorA,get(path).param(invalid[0],invalid[1])).andExpect(status().isBadRequest());
    }

    @Test
    void frontendCanReadOnlyActivePublicCatalogWithCors() throws Exception {
        Servicos inativo = servico(vendedorA);
        inativo.desativar();
        servicos.saveAndFlush(inativo);
        Profissional profissionalInativo = new Profissional(vendedorA.getEstabelecimento(), "Indisponível");
        profissionalInativo.desativar();
        profissionais.saveAndFlush(profissionalInativo);
        String base = "/publico/e/" + vendedorA.getEstabelecimento().getCodigoPublico();

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
        mvc.perform(get("/publico/estabelecimentos/" + vendedorA.getEstabelecimento().getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicBookingLinkUsesOpaqueCodeInsteadOfInternalEstablishmentId() throws Exception {
        String codigo = vendedorA.getEstabelecimento().getCodigoPublico();
        String slug = vendedorA.getEstabelecimento().getSlugPublico();

        assertThat(codigo).isNotBlank().isNotEqualTo(vendedorA.getEstabelecimento().getId().toString());
        mvc.perform(get("/publico/e/" + codigo)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vendedorA.getEstabelecimento().getId()));
        mvc.perform(get("/publico/e/" + codigo + "/servicos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(servicoA.getId()));
        as(vendedorA, get("/vendedor/me/link-agendamento")).andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPublico").value(codigo))
                .andExpect(jsonPath("$.slugPublico").value(slug))
                .andExpect(jsonPath("$.urlAgendamento").value("http://localhost:5173/agendar/" + slug));
    }

    @Test
    void sellerPublishesCatalogAppearanceForOtherDevices() throws Exception {
        String publicCode = vendedorA.getEstabelecimento().getCodigoPublico();
        as(vendedorA, put("/vendedor/me/personalizacao").content("""
                {"nome":"Ateliê Aurora","descricao":"Agende seu momento.","corPrimaria":"#335F55",
                 "logo":null,"capa":null,"capaPosicaoX":42,"capaPosicaoY":68,"tema":"{}"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ateliê Aurora"))
                .andExpect(jsonPath("$.corPrimaria").value("#335F55"));

        mvc.perform(get("/publico/e/" + publicCode)).andExpect(status().isOk())
                .andExpect(jsonPath("$.descricaoPublica").value("Agende seu momento."))
                .andExpect(jsonPath("$.temaPublico").value("{}"))
                .andExpect(jsonPath("$.capaPosicaoX").value(42))
                .andExpect(jsonPath("$.capaPosicaoY").value(68));
        as(vendedorB, get("/vendedor/me/personalizacao")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(vendedorB.getEstabelecimento().getNome()));
    }

    @Test
    void rejectsExecutableImageInCatalogAppearance() throws Exception {
        as(vendedorA, put("/vendedor/me/personalizacao").content("""
                {"nome":"Loja segura","descricao":"Teste","corPrimaria":"#335F55",
                 "logo":"data:image/svg+xml;base64,PHNjcmlwdD4=","capa":null,
                 "capaPosicaoX":50,"capaPosicaoY":50}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsScriptDisguisedAsServiceImage() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("foto", "servico.png", "image/png",
                "<script>alert('xss')</script>".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/estabelecimentos/" + vendedorA.getEstabelecimento().getId()
                + "/servicos/" + servicoA.getId() + "/foto").file(arquivo).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }).header("Authorization", "Bearer " + tokens.gerarToken(vendedorA)))
                .andExpect(status().isBadRequest());
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
        reservaA.setData(LocalDate.now());
        agendamentos.saveAndFlush(reservaA);
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
        as(vendedorA, get("/vendedor/me/agendamentos")
                .param("inicio", LocalDate.now().toString()).param("fim", LocalDate.now().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        as(vendedorA, get("/vendedor/me/dashboard")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(vendedorA.getNome()))
                .andExpect(jsonPath("$.servicosAtivos").value(1))
                .andExpect(jsonPath("$.profissionaisAtivos").value(1))
                .andExpect(jsonPath("$.agendamentosHoje").value(1))
                .andExpect(jsonPath("$.proximosAgendamentos[0].id").value(reservaA.getId()));
        as(clienteA, get("/vendedor/me")).andExpect(status().isForbidden());
        as(clienteA, get("/vendedor/me/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    void tokenReturnedByLoginAuthenticatesDashboard() throws Exception {
        String loginResponse = mvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"" + vendedorA.getEmail() + "\",\"senha\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(loginResponse, "$.token");
        mvc.perform(get("/vendedor/me/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(vendedorA.getNome()));
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

        String horarios = "/publico/e/" + vendedorA.getEstabelecimento().getCodigoPublico()
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
    void ownerCanReplaceExistingProfessionalAvailabilityMoreThanOnce() throws Exception {
        String gestao = "/vendedor/" + vendedorA.getId() + "/profissionais/"
                + vendedorA.getProfissionalPrincipal().getId() + "/disponibilidade";

        as(vendedorA, put(gestao).content("""
                {"jornadas":[{"diaSemana":"MONDAY","horaInicio":"09:00:00","horaFim":"17:00:00"}]}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jornadas.length()").value(1))
                .andExpect(jsonPath("$.jornadas[0].horaInicio").value("09:00:00"));

        as(vendedorA, put(gestao).content("""
                {"jornadas":[{"diaSemana":"MONDAY","horaInicio":"10:00:00","horaFim":"18:00:00"}]}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jornadas.length()").value(1))
                .andExpect(jsonPath("$.jornadas[0].horaInicio").value("10:00:00"))
                .andExpect(jsonPath("$.jornadas[0].horaFim").value("18:00:00"));
    }

    @Test
    void ownerCanBlockOwnScheduleAndPublicBookingRespectsTheBlock() throws Exception {
        Long profissionalId = vendedorA.getProfissionalPrincipal().getId();
        String base = "/vendedor/me/profissionais/" + profissionalId + "/bloqueios";
        String created = as(vendedorA, post(base).content("""
                {"tipo":"PAUSA","dataInicio":"2027-01-04","dataFim":"2027-01-04",
                 "horaInicio":"09:30:00","horaFim":"10:30:00","motivo":"Almoço","recorrente":false}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PAUSA"))
                .andExpect(jsonPath("$.profissionalId").value(profissionalId))
                .andReturn().getResponse().getContentAsString();
        Number bloqueioId = JsonPath.read(created, "$.id");

        as(vendedorA, get(base)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motivo").value("Almoço"));
        String horarios = "/publico/e/" + vendedorA.getEstabelecimento().getCodigoPublico()
                + "/profissionais/" + profissionalId
                + "/horarios?servicoId=" + servicoA.getId() + "&data=2027-01-04";
        mvc.perform(get(horarios)).andExpect(status().isOk())
                .andExpect(jsonPath("$.horariosBloqueados").isArray())
                .andExpect(jsonPath("$.horariosBloqueados[?(@ == '10:00:00')]").exists());
        as(clienteA, post("/agendamento").content(booking(clienteA.getId(), vendedorA, servicoA)))
                .andExpect(status().isConflict());

        as(vendedorB, delete(base + "/" + bloqueioId.longValue())).andExpect(status().isNotFound());
        as(vendedorA, delete(base + "/" + bloqueioId.longValue())).andExpect(status().isNoContent());
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
    void meRoutesDeriveTenantFromTokenAndRejectForeignResourceIds() throws Exception {
        String servicosMe = "/vendedor/me/servicos";
        String profissionaisMe = "/vendedor/me/profissionais";

        as(vendedorA, get(servicosMe)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(servicoA.getId()));
        as(vendedorA, put(servicosMe + "/" + servicoB.getId()).content("""
                {"nome":"Invasão","descricao":"","preco":10,"tempo":"00:30:00","profissionaisIds":[%d]}
                """.formatted(vendedorA.getProfissionalPrincipal().getId())))
                .andExpect(status().isNotFound());
        as(vendedorA, patch(servicosMe + "/" + servicoB.getId() + "/desativar"))
                .andExpect(status().isNotFound());

        as(vendedorA, get(profissionaisMe)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(vendedorA.getProfissionalPrincipal().getId()));
        as(vendedorA, put(profissionaisMe + "/" + vendedorB.getProfissionalPrincipal().getId())
                .content("{\"nome\":\"Invasão\"}"))
                .andExpect(status().isNotFound());
        as(vendedorA, get(profissionaisMe + "/" + vendedorB.getProfissionalPrincipal().getId() + "/disponibilidade"))
                .andExpect(status().isNotFound());

        assertThat(servicos.findById(servicoB.getId()).orElseThrow().isAtivo()).isTrue();
        assertThat(profissionais.findById(vendedorB.getProfissionalPrincipal().getId()).orElseThrow().getNome())
                .doesNotContain("Invasão");
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
        Assinatura assinatura = assinaturas.findByEstabelecimentoId(novo.getEstabelecimento().getId()).orElseThrow();
        assertThat(assinatura.getPlano()).isEqualTo(PlanoAssinatura.GRATUITO);
        assertThat(assinatura.getStatus()).isEqualTo(StatusAssinatura.ATIVA);
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
        Estabelecimento estabelecimento = estabelecimentos.save(new Estabelecimento(email + " - estabelecimento", "America/Sao_Paulo"));
        assinaturas.save(Assinatura.testeGratis(estabelecimento,
                java.time.Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS)));
        v.vincularEstabelecimento(estabelecimento);
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
