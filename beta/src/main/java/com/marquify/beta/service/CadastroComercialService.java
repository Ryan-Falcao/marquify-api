package com.marquify.beta.service;

import com.marquify.beta.entity.DiasAbertos;
import com.marquify.beta.entity.DisponibilidadeProfissional;
import com.marquify.beta.entity.Estabelecimento;
import com.marquify.beta.entity.Profissional;
import com.marquify.beta.entity.UserRole;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.infra.security.TokenService;
import com.marquify.beta.repository.DisponibilidadeProfissionalRepository;
import com.marquify.beta.repository.EstabelecimentoRepository;
import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.CadastroComercialRequest;
import com.marquify.beta.response.CadastroComercialResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;

@Service
@AllArgsConstructor
@Transactional
public class CadastroComercialService {
    private final clienteRepository clientes;
    private final vendedorRepository vendedores;
    private final EstabelecimentoRepository estabelecimentos;
    private final com.marquify.beta.repository.ProfissionalRepository profissionais;
    private final DisponibilidadeProfissionalRepository disponibilidades;
    private final AssinaturaService assinaturas;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public CadastroComercialResponse cadastrar(CadastroComercialRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (clientes.existsByEmailIgnoreCase(email) || vendedores.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já está em uso");
        }
        Estabelecimento estabelecimento = estabelecimentos.save(new Estabelecimento(request.estabelecimento(), request.fusoHorario()));
        assinaturas.criarAssinaturaGratuita(estabelecimento);
        Vendedor vendedor = new Vendedor();
        vendedor.setNome(request.nome().trim());
        vendedor.setEmail(email);
        vendedor.setNomeLoja(estabelecimento.getNome());
        vendedor.setHoraAbertura(LocalTime.of(9, 0));
        vendedor.setHoraFechamento(LocalTime.of(18, 0));
        vendedor.setDiasAbertos(EnumSet.of(DiasAbertos.Segunda, DiasAbertos.Terca, DiasAbertos.Quarta,
                DiasAbertos.Quinta, DiasAbertos.Sexta, DiasAbertos.Sabado));
        vendedor.setSenha(passwordEncoder.encode(request.senha()));
        vendedor.setRole(UserRole.ADMIN);
        vendedor.vincularEstabelecimento(estabelecimento);
        vendedor = vendedores.save(vendedor);

        Profissional principal = profissionais.save(new Profissional(estabelecimento, vendedor.getNome()));
        vendedor.vincularProfissionalPrincipal(principal);
        vendedor = vendedores.save(vendedor);
        for (DayOfWeek dia : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY)) {
            disponibilidades.save(new DisponibilidadeProfissional(principal, dia, LocalTime.of(9, 0), LocalTime.of(18, 0)));
        }
        return new CadastroComercialResponse(tokens.gerarToken(vendedor), estabelecimento.getId(), vendedor.getId());
    }
}
