package com.marquify.beta.service;

import com.marquify.beta.entity.ContaProfissional;
import com.marquify.beta.entity.Profissional;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.infra.security.TokenService;
import com.marquify.beta.repository.ContaProfissionalRepository;
import com.marquify.beta.repository.ProfissionalRepository;
import com.marquify.beta.repository.clienteRepository;
import com.marquify.beta.repository.vendedorRepository;
import com.marquify.beta.request.AceitarConviteProfissionalRequest;
import com.marquify.beta.request.ConviteProfissionalRequest;
import com.marquify.beta.response.ConviteProfissionalResponse;
import com.marquify.beta.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
public class ConviteProfissionalService {
    private final CurrentUser currentUser; private final ProfissionalRepository profissionais; private final ContaProfissionalRepository contas;
    private final vendedorRepository vendedores; private final clienteRepository clientes; private final PasswordEncoder passwords; private final TokenService tokens;
    @Value("${api.public-web-url:http://localhost:5173}") private String webUrl;

    public ConviteProfissionalResponse convidar(Long profissionalId, ConviteProfissionalRequest request) {
        var dono = currentUser.vendedor();
        Profissional profissional = profissionais.findByIdAndEstabelecimentoId(profissionalId, dono.getEstabelecimento().getId()).orElseThrow(this::notFound);
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (vendedores.existsByEmailIgnoreCase(email) || clientes.existsByEmailIgnoreCase(email) || contas.existsByEmailIgnoreCaseAndProfissionalIdNot(email, profissionalId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já está em uso");
        String token = UUID.randomUUID().toString().replace("-", ""); Instant expira = Instant.now().plus(7, ChronoUnit.DAYS);
        ContaProfissional conta = contas.findByProfissionalId(profissionalId).map(atual -> { atual.renovarConvite(email, token, expira); return atual; })
                .orElseGet(() -> new ContaProfissional(profissional, email, token, expira));
        conta = contas.save(conta); String url = webUrl.replaceAll("/+$", "") + "/convite-profissional/" + token;
        return ConviteProfissionalResponse.from(conta, url);
    }
    @Transactional(readOnly = true) public ConviteProfissionalResponse consultar(String token) {
        ContaProfissional conta = convite(token); return ConviteProfissionalResponse.from(conta, null);
    }
    public LoginResponse aceitar(String token, AceitarConviteProfissionalRequest request) {
        ContaProfissional conta = convite(token); conta.aceitar(passwords.encode(request.senha())); contas.save(conta); return new LoginResponse(tokens.gerarToken(conta));
    }
    private ContaProfissional convite(String token) { ContaProfissional conta = contas.findByConviteToken(token).orElseThrow(this::notFound); if (!conta.conviteValido()) throw new ResponseStatusException(HttpStatus.GONE, "Este convite expirou. Peça um novo convite."); return conta; }
    private ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Convite não encontrado"); }
}
