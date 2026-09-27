package com.marquify.beta.service;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Status;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.DashboardFiltro;
import com.marquify.beta.response.DashboardConsultaResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardConsultaService {
    private final CurrentUser currentUser;
    private final vendedorRepository vendedores;
    private final EntityManager em;
    private final Clock clock;
    private static final Set<String> STATUS = Set.of("AGENDADO", "PREVISTO", "EM_ATENDIMENTO", "FINALIZADO", "CANCELADO");
    private static final Set<String> ORDENS = Set.of("data", "horaInicio", "valorCobrado", "cliente", "profissional", "id");
    private record Consulta(Long estabelecimentoId, DashboardFiltro filtro, Periodo periodo, int pagina, int tamanho, String ordem, String direcao) {}

    private Consulta consulta(DashboardFiltro f) {
        var vendedor = vendedores.findById(currentUser.vendedor().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var estabelecimento = vendedor.getEstabelecimento();
        var agora = ZonedDateTime.now(clock.withZone(ZoneId.of(estabelecimento.getFusoHorario())));
        if ((f.inicio() == null) != (f.fim() == null)) throw invalid("Informe início e fim juntos");
        LocalDate inicio = f.inicio() == null ? agora.toLocalDate() : f.inicio();
        LocalDate fim = f.fim() == null ? inicio : f.fim();
        if (fim.isBefore(inicio)) throw invalid("Fim deve ser igual ou posterior ao início");
        if (f.profissionalId() != null && f.profissionalId() <= 0 || f.clienteId() != null && f.clienteId() <= 0) throw invalid("Identificador inválido");
        if (f.status() != null && !STATUS.contains(f.status())) throw invalid("Status inválido");
        int pagina = f.pagina() == null ? 0 : f.pagina();
        int tamanho = f.tamanho() == null ? 20 : f.tamanho();
        if (pagina < 0 || tamanho < 1 || tamanho > 100 || (long) pagina * tamanho > Integer.MAX_VALUE) throw invalid("Página inválida; tamanho deve estar entre 1 e 100");
        String ordem = f.ordenarPor() == null ? "data" : f.ordenarPor();
        String direcao = f.direcao() == null ? "DESC" : f.direcao().toUpperCase(Locale.ROOT);
        if (!ORDENS.contains(ordem) || !Set.of("ASC", "DESC").contains(direcao)) throw invalid("Ordenação inválida");
        return new Consulta(estabelecimento.getId(), f, new Periodo(inicio, fim, estabelecimento.getFusoHorario(), agora), pagina, tamanho, ordem, direcao);
    }

    private Predicate estado(CriteriaBuilder cb, Root<Agendamento> r, String status, Consulta c) {
        var hoje = c.periodo().referencia().toLocalDate();
        var hora = c.periodo().referencia().toLocalTime();
        var ativo = cb.equal(r.get("status"), Status.AGENDADO);
        var terminou = cb.or(cb.lessThan(r.<LocalDate>get("data"), hoje), cb.and(cb.equal(r.get("data"), hoje), cb.lessThanOrEqualTo(r.<LocalTime>get("horaFim"), hora)));
        var comecou = cb.or(cb.lessThan(r.<LocalDate>get("data"), hoje), cb.and(cb.equal(r.get("data"), hoje), cb.lessThanOrEqualTo(r.<LocalTime>get("horaInicio"), hora)));
        return switch (status) {
            case "CANCELADO" -> cb.equal(r.get("status"), Status.CANCELADO);
            case "FINALIZADO" -> cb.and(ativo, terminou);
            case "EM_ATENDIMENTO" -> cb.and(ativo, comecou, cb.not(terminou));
            case "PREVISTO" -> cb.and(ativo, cb.not(comecou));
            default -> ativo;
        };
    }

    private Predicate filtro(CriteriaBuilder cb, Root<Agendamento> r, Consulta c) {
        List<Predicate> p = new ArrayList<>();
        p.add(cb.equal(r.get("estabelecimento").get("id"), c.estabelecimentoId()));
        p.add(cb.between(r.get("data"), c.periodo().inicio(), c.periodo().fim()));
        if (c.filtro().profissionalId() != null) p.add(cb.equal(r.get("profissional").get("id"), c.filtro().profissionalId()));
        if (c.filtro().clienteId() != null) p.add(cb.equal(r.get("cliente").get("id"), c.filtro().clienteId()));
        if (c.filtro().status() != null) p.add(estado(cb,r,c.filtro().status(),c));
        return cb.and(p.toArray(Predicate[]::new));
    }

    public Pagina agendamentos(DashboardFiltro filtro) {
        Consulta c = consulta(filtro);
        var cb = em.getCriteriaBuilder();
        var count = cb.createQuery(Long.class); var cr = count.from(Agendamento.class);
        count.select(cb.count(cr)).where(filtro(cb,cr,c));
        long total = em.createQuery(count).getSingleResult();
        var query = cb.createQuery(Agendamento.class); var root = query.from(Agendamento.class);
        root.fetch("cliente", JoinType.LEFT); root.fetch("profissional", JoinType.LEFT); root.fetch("servico", JoinType.LEFT);
        Expression<?> campo = switch (c.ordem()) {
            case "cliente", "profissional" -> root.join(c.ordem(), JoinType.LEFT).get("nome");
            default -> root.get(c.ordem());
        };
        boolean asc = c.direcao().equals("ASC");
        query.select(root).where(filtro(cb,root,c)).orderBy(asc ? cb.asc(campo) : cb.desc(campo),
                asc ? cb.asc(root.get("horaInicio")) : cb.desc(root.get("horaInicio")),
                asc ? cb.asc(root.get("id")) : cb.desc(root.get("id")));
        var items = em.createQuery(query).setFirstResult(c.pagina()*c.tamanho()).setMaxResults(c.tamanho()).getResultList()
                .stream().map(a -> item(a,c)).toList();
        return new Pagina(c.periodo(),items,c.pagina(),c.tamanho(),total,(total+c.tamanho()-1)/c.tamanho(),c.ordem(),c.direcao());
    }

    private Item item(Agendamento a, Consulta c) {
        var agora = c.periodo().referencia().toLocalDateTime();
        String status = a.getStatus() == Status.CANCELADO ? "CANCELADO" : !a.getData().atTime(a.getHoraFim()).isAfter(agora) ? "FINALIZADO"
                : !a.getData().atTime(a.getHoraInicio()).isAfter(agora) ? "EM_ATENDIMENTO" : "PREVISTO";
        return new Item(a.getId(),a.getData(),a.getHoraInicio(),a.getHoraFim(),status,
                a.getCliente() == null ? null : a.getCliente().getId(),a.getCliente() == null ? null : a.getCliente().getNome(),
                a.getProfissional().getId(),a.getProfissional().getNome(),
                a.getServico() == null ? null : a.getServico().getId(),a.getServico() == null ? null : a.getServico().getNome(),
                a.getValorCobrado(),Duration.between(a.getHoraInicio(),a.getHoraFim()).toMinutes());
    }

    public Indicadores indicadores(DashboardFiltro filtro) {
        Consulta c = consulta(filtro);
        var cb = em.getCriteriaBuilder(); var query = cb.createTupleQuery(); var r = query.from(Agendamento.class);
        Predicate previsto = estado(cb,r,"PREVISTO",c), andamento = estado(cb,r,"EM_ATENDIMENTO",c), finalizado = estado(cb,r,"FINALIZADO",c), cancelado = estado(cb,r,"CANCELADO",c);
        query.multiselect(cb.count(r), quantidade(cb,previsto), quantidade(cb,andamento), quantidade(cb,finalizado), quantidade(cb,cancelado),
                valor(cb,r,finalizado), valor(cb,r,cb.or(previsto,andamento)), quantidade(cb,cb.isNull(r.get("valorCobrado"))))
                .where(filtro(cb,r,c));
        var t = em.createQuery(query).getSingleResult();
        return new Indicadores(c.periodo(),t.get(0,Long.class),t.get(1,Long.class),t.get(2,Long.class),t.get(3,Long.class),t.get(4,Long.class),t.get(5,BigDecimal.class),t.get(6,BigDecimal.class),t.get(7,Long.class));
    }

    private Expression<Long> quantidade(CriteriaBuilder cb, Predicate p) { return cb.coalesce(cb.sum(cb.<Long>selectCase().when(p,1L).otherwise(0L)),0L); }
    private Expression<BigDecimal> valor(CriteriaBuilder cb, Root<Agendamento> r, Predicate p) {
        return cb.coalesce(cb.sum(cb.<BigDecimal>selectCase().when(p,cb.coalesce(r.<BigDecimal>get("valorCobrado"),BigDecimal.ZERO)).otherwise(BigDecimal.ZERO)),BigDecimal.ZERO);
    }
    private ResponseStatusException invalid(String text) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,text); }
}
