package com.marquify.beta;

import com.marquify.beta.entity.*;
import com.marquify.beta.repository.*;
import com.marquify.beta.infra.security.TokenService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingLifecycleTests {
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired PlatformTransactionManager transactions;
    @Autowired vendedorRepository vendedores;
    @Autowired clienteRepository clientes;
    @Autowired EstabelecimentoRepository estabelecimentos;
    @Autowired ProfissionalRepository profissionais;
    @Autowired DisponibilidadeProfissionalRepository jornadas;
    @Autowired servicoRepository servicos;
    @Autowired agendamentoRepository agendamentos;

    record Fixture(long vendedor, long profissional, long servico, String token, String otherToken, String adminToken, LocalDate data) {}
    Fixture fixture() {
        return new TransactionTemplate(transactions).execute(tx -> {
            String key = UUID.randomUUID().toString();
            var e = estabelecimentos.save(new Estabelecimento("Loja " + key, "America/Sao_Paulo"));
            var v = new Vendedor(); v.setNome("Responsável"); v.setEmail(key + "@seller.test"); v.setSenha("unused");
            v.setNomeLoja(e.getNome()); v.setEstabelecimento(e); v.setRole(UserRole.ADMIN);
            v.setHoraAbertura(LocalTime.of(8,0)); v.setHoraFechamento(LocalTime.of(18,0)); v.setDiasAbertos(Set.of(DiasAbertos.Segunda));
            vendedores.save(v);
            var p = profissionais.save(new Profissional(e, "Profissional"));
            LocalDate date = LocalDate.now().plusDays(7);
            jornadas.save(new DisponibilidadeProfissional(p, date.getDayOfWeek(), LocalTime.of(8,0), LocalTime.of(18,0)));
            var s = new Servicos(); s.setNome("Corte"); s.setPreco(30.0); s.setTempo(LocalTime.of(0,30));
            s.setEstabelecimento(e); s.setVendedor(v); s.definirProfissionais(Set.of(p)); servicos.save(s);
            var a = clientes.save(new Cliente(key + "@a.test", "unused"));
            var b = clientes.save(new Cliente(key + "@b.test", "unused"));
            return new Fixture(v.getId(), p.getId(), s.getId(), tokens.gerarToken(a), tokens.gerarToken(b), tokens.gerarToken(v), date);
        });
    }
    String body(Fixture f, String time) {
        return "{\"vendedorId\":"+f.vendedor()+",\"profissionalId\":"+f.profissional()+",\"servicoId\":"+f.servico()+",\"data\":\""+f.data()+"\",\"horaInicio\":\""+time+"\"}";
    }
    long book(Fixture f, String time) throws Exception {
        String body = mvc.perform(post("/agendamento").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f,time))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
    @Test
    void concurrentOverlappingReservationsHaveOnlyOneWinner() throws Exception {
        var f = fixture();
        var gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var futures = new ArrayList<Future<Integer>>();
            for (String time : List.of("10:00", "10:15")) futures.add(executor.submit(() -> {
                gate.await();
                return mvc.perform(post("/agendamento").header("Authorization", "Bearer " + f.token())
                        .contentType("application/json").content(body(f,time))).andReturn().getResponse().getStatus();
            }));
            gate.countDown();
            assertThat(List.of(futures.get(0).get(15, TimeUnit.SECONDS), futures.get(1).get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 409);
        }
    }
    @Test
    void concurrentReschedulesCannotOccupySameInterval() throws Exception {
        var f = fixture(); long first = book(f,"10:00"); long second = book(f,"11:00");
        var gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var futures = new ArrayList<Future<Integer>>();
            for (long id : List.of(first, second)) futures.add(executor.submit(() -> {
                gate.await();
                return mvc.perform(put("/agendamento/"+id+"/remarcar").header("Authorization", "Bearer " + f.token())
                        .contentType("application/json").content(body(f,"12:00"))).andReturn().getResponse().getStatus();
            }));
            gate.countDown();
            assertThat(List.of(futures.get(0).get(15, TimeUnit.SECONDS), futures.get(1).get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 409);
        }
    }
    @Test
    void reschedulePreservesContractAndConflictPreservesOriginalThenCancellationReleasesSlot() throws Exception {
        var f = fixture(); long id = book(f,"10:00"); book(f,"11:00");
        new TransactionTemplate(transactions).executeWithoutResult(tx -> {
            var service = servicos.findById(f.servico()).orElseThrow(); service.setPreco(99.0); service.setTempo(LocalTime.of(1,0));
        });
        mvc.perform(put("/agendamento/"+id+"/remarcar").header("Authorization", "Bearer " + f.otherToken())
                .contentType("application/json").content(body(f,"12:00"))).andExpect(status().isNotFound());
        mvc.perform(put("/agendamento/"+id+"/remarcar").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f,"11:15"))).andExpect(status().isConflict());
        assertThat(agendamentos.findById(id).orElseThrow().getHoraInicio()).isEqualTo(LocalTime.of(10,0));
        mvc.perform(put("/agendamento/"+id+"/remarcar").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f,"12:00"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.valorCobrado").value(30)).andExpect(jsonPath("$.duracaoMinutos").value(30))
                .andExpect(jsonPath("$.horaFim").value("12:30:00"));
        for (int i=0; i<2; i++) mvc.perform(put("/agendamento/cancelar").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content("{\"agendamentoId\":"+id+"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(put("/agendamento/"+id+"/remarcar").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f,"13:00"))).andExpect(status().isConflict());
        book(f,"12:00");
    }
    @Test
    void pastAppointmentsCannotBeChangedAndMidnightOverflowIsRejected() throws Exception {
        var f = fixture(); long id = book(f,"10:00");
        new TransactionTemplate(transactions).executeWithoutResult(tx -> agendamentos.findById(id).orElseThrow().setData(LocalDate.now().minusDays(1)));
        mvc.perform(put("/agendamento/cancelar").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content("{\"agendamentoId\":"+id+"}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/agendamento/"+id+"/remarcar").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f,"12:00"))).andExpect(status().isConflict());
        mvc.perform(post("/agendamento").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f,"23:45"))).andExpect(status().isConflict());
    }

    @Test
    void archivedServicesAndProfessionalsKeepHistoryAndRejectNewBookings() throws Exception {
        var f = fixture(); long appointmentId = book(f, "10:00");
        mvc.perform(patch("/vendedor/me/servicos/" + f.servico() + "/arquivar").header("Authorization", "Bearer " + f.adminToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(post("/agendamento").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f, "11:00"))).andExpect(status().isNotFound());
        assertThat(agendamentos.findById(appointmentId)).isPresent();
        mvc.perform(patch("/vendedor/me/servicos/" + f.servico() + "/restaurar").header("Authorization", "Bearer " + f.adminToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(patch("/vendedor/me/profissionais/" + f.profissional() + "/arquivar").header("Authorization", "Bearer " + f.adminToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(post("/agendamento").header("Authorization", "Bearer " + f.token())
                .contentType("application/json").content(body(f, "11:00"))).andExpect(status().isNotFound());
        assertThat(agendamentos.findById(appointmentId)).isPresent();
        mvc.perform(patch("/vendedor/me/profissionais/" + f.profissional() + "/restaurar").header("Authorization", "Bearer " + f.adminToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
    }
}
