package com.marquify.beta.service;

import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.entity.Servicos;
import com.marquify.beta.entity.Profissional;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.*;
import com.marquify.beta.request.*;
import com.marquify.beta.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class VendedorService {
    private final vendedorRepository vendedores;
    private final agendamentoRepository agendamentos;
    private final servicoRepository servicos;
    private final ProfissionalRepository profissionais;
    private final CurrentUser currentUser;

    @Value("${api.public-web-url:http://localhost:5173}")
    private String publicWebUrl;

    private Vendedor ownVendedor(Long id) {
        currentUser.vendedor(id);
        return vendedores.findById(id).orElseThrow(this::notFound);
    }

    private Vendedor vendedorAtual() {
        Vendedor atual = currentUser.vendedor();
        return vendedores.findById(atual.getId()).orElseThrow(this::notFound);
    }

    @Transactional(readOnly = true)
    public VendedorResponse getMyInfos(Long id) {
        return VendedorResponse.from(ownVendedor(id));
    }

    @Transactional(readOnly = true)
    public VendedorResponse getMyInfos() {
        return VendedorResponse.from(vendedorAtual());
    }

    @Transactional(readOnly = true)
    public LinkAgendamentoResponse linkAgendamento() {
        String codigoPublico = vendedorAtual().getEstabelecimento().getCodigoPublico();
        String base = publicWebUrl.replaceAll("/+$", "");
        return new LinkAgendamentoResponse(codigoPublico, base + "/agendar/" + codigoPublico);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> getAgendamentos(VendedorRequest request) {
        return getAgendamentos(request.getVendedor_id());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> getAgendamentos(Long vendedorId) {
        Vendedor vendedor = ownVendedor(vendedorId);
        return agendamentos.findByVendedorId(vendedor.getId()).stream().map(AgendamentoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> getAgendamentos() {
        Vendedor vendedor = vendedorAtual();
        return agendamentos.findByVendedorId(vendedor.getId()).stream().map(AgendamentoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> getAgendamentos(LocalDate inicio, LocalDate fim) {
        Vendedor vendedor = vendedorAtual();
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intervalo de datas inválido");
        }
        return agendamentos.findAllByVendedorIdAndDataBetweenOrderByDataAscHoraInicioAsc(vendedor.getId(), inicio, fim)
                .stream().map(AgendamentoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        Vendedor vendedor = vendedorAtual();
        Long estabelecimentoId = vendedor.getEstabelecimento().getId();
        ZonedDateTime agora = ZonedDateTime.now(ZoneId.of(vendedor.getEstabelecimento().getFusoHorario()));
        LocalDate hoje = agora.toLocalDate();
        List<AgendamentoResponse> proximosAgendamentos = agendamentos
                .findAllByVendedorIdAndDataAndStatusOrderByHoraInicioAsc(vendedor.getId(), hoje, com.marquify.beta.entity.Status.AGENDADO)
                .stream().map(AgendamentoResponse::from).toList();

        return new DashboardResponse(
                vendedor.getNome(),
                servicos.countByEstabelecimentoIdAndAtivoTrue(estabelecimentoId),
                profissionais.countByEstabelecimentoIdAndAtivoTrue(estabelecimentoId),
                proximosAgendamentos.size(),
                agendamentos.totalFinalizado(vendedor.getId(), com.marquify.beta.entity.Status.AGENDADO,
                        hoje, agora.toLocalTime()),
                proximosAgendamentos
        );
    }

    public VendedorResponse mudarNome(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewNome() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setNome(request.getNewNome());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarNomeLoja(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewNomeLoja() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setNomeLoja(request.getNewNomeLoja());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarDiasAbertos(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewDiasAbertos() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setDiasAbertos(request.getNewDiasAbertos());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarHoraAbertura(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewHoraAbertura() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setHoraAbertura(request.getNewHoraAbertura());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public VendedorResponse mudarHoraFechamento(VendedorRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedor_id());
        if (request.getNewHoraFechamento() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo obrigatório");
        vendedor.setHoraFechamento(request.getNewHoraFechamento());
        return VendedorResponse.from(vendedores.save(vendedor));
    }

    public ServicoResponse criarServico(ServicoRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedorId());
        Set<Profissional> profissionaisDoServico = profissionaisDoEstabelecimento(request.getProfissionaisIds(), vendedor);
        Servicos servico = new Servicos();
        servico.setNome(request.getNome());
        servico.setDescricao(request.getDescricao());
        servico.setPreco(request.getPreco());
        servico.setTempo(request.getTempo());
        servico.setEstabelecimento(vendedor.getEstabelecimento());
        servico.setVendedor(vendedor);
        servico.definirProfissionais(profissionaisDoServico);
        return ServicoResponse.from(servicos.save(servico));
    }

    public void deletarServico(ServicoRequest request) {
        Vendedor vendedor = ownVendedor(request.getVendedorId());
        if (request.getServicoId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço é obrigatório");
        Servicos servico = servicos.findByIdAndEstabelecimentoId(request.getServicoId(), vendedor.getEstabelecimento().getId())
                .orElseThrow(this::notFound);
        servico.desativar();
        servicos.save(servico);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listarServicos(Long estabelecimentoId) {
        vendedorDoEstabelecimento(estabelecimentoId);
        return servicos.findAllByEstabelecimentoIdOrderByNomeAsc(estabelecimentoId).stream()
                .map(ServicoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listarMeusServicos() {
        Vendedor vendedor = vendedorAtual();
        return servicos.findAllByEstabelecimentoIdOrderByNomeAsc(vendedor.getEstabelecimento().getId()).stream()
                .map(ServicoResponse::from).toList();
    }

    public ServicoResponse criarServicoNoEstabelecimento(Long estabelecimentoId, ServicoCatalogoRequest request) {
        Vendedor vendedor = vendedorDoEstabelecimento(estabelecimentoId);
        Servicos servico = new Servicos();
        preencherServico(servico, request, vendedor);
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse criarServicoAtual(ServicoCatalogoRequest request) {
        Vendedor vendedor = vendedorAtual();
        Servicos servico = new Servicos();
        preencherServico(servico, request, vendedor);
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse atualizarServicoAtual(Long servicoId, ServicoCatalogoRequest request) {
        Vendedor vendedor = vendedorAtual();
        Servicos servico = servicoDoEstabelecimento(servicoId, vendedor.getEstabelecimento().getId());
        preencherServico(servico, request, vendedor);
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse ativarServicoAtual(Long servicoId) {
        Vendedor vendedor = vendedorAtual();
        Servicos servico = servicoDoEstabelecimento(servicoId, vendedor.getEstabelecimento().getId());
        servico.ativar();
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse desativarServicoAtual(Long servicoId) {
        Vendedor vendedor = vendedorAtual();
        Servicos servico = servicoDoEstabelecimento(servicoId, vendedor.getEstabelecimento().getId());
        servico.desativar();
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse atualizarServico(Long estabelecimentoId, Long servicoId, ServicoCatalogoRequest request) {
        Vendedor vendedor = vendedorDoEstabelecimento(estabelecimentoId);
        Servicos servico = servicoDoEstabelecimento(servicoId, estabelecimentoId);
        preencherServico(servico, request, vendedor);
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse ativarServico(Long estabelecimentoId, Long servicoId) {
        vendedorDoEstabelecimento(estabelecimentoId);
        Servicos servico = servicoDoEstabelecimento(servicoId, estabelecimentoId);
        servico.ativar();
        return ServicoResponse.from(servicos.save(servico));
    }

    public ServicoResponse desativarServico(Long estabelecimentoId, Long servicoId) {
        vendedorDoEstabelecimento(estabelecimentoId);
        Servicos servico = servicoDoEstabelecimento(servicoId, estabelecimentoId);
        servico.desativar();
        return ServicoResponse.from(servicos.save(servico));
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }

    private Set<Profissional> profissionaisDoEstabelecimento(Set<Long> profissionaisIds, Vendedor vendedor) {
        if (profissionaisIds == null || profissionaisIds.isEmpty() || profissionaisIds.contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um profissional");
        }
        List<Profissional> encontrados = profissionais.findAllById(profissionaisIds);
        boolean todosPertencemAoEstabelecimento = encontrados.size() == profissionaisIds.size()
                && encontrados.stream().allMatch(profissional -> profissional.isAtivo()
                && profissional.getEstabelecimento().getId().equals(vendedor.getEstabelecimento().getId()));
        if (!todosPertencemAoEstabelecimento) {
            throw notFound();
        }
        return new LinkedHashSet<>(encontrados);
    }

    private void preencherServico(Servicos servico, ServicoCatalogoRequest request, Vendedor vendedor) {
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setPreco(request.preco());
        servico.setTempo(request.tempo());
        servico.setEstabelecimento(vendedor.getEstabelecimento());
        if (servico.getVendedor() == null) {
            servico.setVendedor(vendedor);
        }
        servico.definirProfissionais(profissionaisDoEstabelecimento(request.profissionaisIds(), vendedor));
    }

    private Servicos servicoDoEstabelecimento(Long servicoId, Long estabelecimentoId) {
        return servicos.findByIdAndEstabelecimentoId(servicoId, estabelecimentoId).orElseThrow(this::notFound);
    }

    private Vendedor vendedorDoEstabelecimento(Long estabelecimentoId) {
        if (!(currentUser.principal() instanceof Vendedor atual)) {
            throw new AccessDeniedException("Acesso negado");
        }
        currentUser.vendedor(atual.getId());
        Vendedor vendedor = vendedores.findById(atual.getId()).orElseThrow(this::notFound);
        if (!vendedor.getEstabelecimento().getId().equals(estabelecimentoId)) {
            throw new AccessDeniedException("Acesso negado");
        }
        return vendedor;
    }
}
