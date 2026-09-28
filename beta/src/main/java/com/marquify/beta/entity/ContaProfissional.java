package com.marquify.beta.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "contas_profissionais")
@Getter
@NoArgsConstructor
public class ContaProfissional implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "profissional_id", nullable = false, unique = true) private Profissional profissional;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @JsonIgnore private String senha;
    @Column(name = "convite_token", unique = true, length = 64) private String conviteToken;
    @Column(name = "convite_expira_em") private Instant conviteExpiraEm;
    @Column(name = "aceito_em") private Instant aceitoEm;
    @Column(name = "criado_em", nullable = false, updatable = false) private Instant criadoEm;

    public ContaProfissional(Profissional profissional, String email, String token, Instant expiraEm) {
        this.profissional = profissional; this.email = email.trim().toLowerCase(java.util.Locale.ROOT);
        this.conviteToken = token; this.conviteExpiraEm = expiraEm; this.criadoEm = Instant.now();
    }
    public void renovarConvite(String email, String token, Instant expiraEm) { this.email = email.trim().toLowerCase(java.util.Locale.ROOT); this.conviteToken = token; this.conviteExpiraEm = expiraEm; this.aceitoEm = null; this.senha = null; }
    public void aceitar(String senha) { this.senha = senha; this.aceitoEm = Instant.now(); this.conviteToken = null; this.conviteExpiraEm = null; }
    public boolean conviteValido() { return conviteToken != null && conviteExpiraEm != null && conviteExpiraEm.isAfter(Instant.now()); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_PROFISSIONAL")); }
    @Override @JsonIgnore public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return aceitoEm != null && profissional != null && profissional.isAtivo(); }
}
