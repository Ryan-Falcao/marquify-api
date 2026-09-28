package com.marquify.beta.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "vendedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vendedor implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nome;

    @NotNull
    private String email;

    /**
     * Relação de transição: cada conta de proprietário tem um estabelecimento.
     * Profissionais são modelados em entidade própria, sem reutilizar esta conta.
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false, unique = true)
    private Estabelecimento estabelecimento;

    /** Profissional correspondente ao proprietário nos dados legados, quando ele atende clientes. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_principal_id", unique = true)
    private Profissional profissionalPrincipal;

    /** @deprecated Dados do negócio serão migrados gradualmente para Estabelecimento. */
    @Deprecated(forRemoval = false)
    @NotNull
    private String nomeLoja;

    @NotNull
    private LocalTime horaAbertura;

    @NotNull
    private LocalTime horaFechamento;

    @NotNull
    @ElementCollection(targetClass = DiasAbertos.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "vendedor_dias_abertos", joinColumns = @JoinColumn(name = "vendedor_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia", length = 20)
    private Set<DiasAbertos> diasAbertos;

    @NotNull
    @JsonIgnore
    private String senha;

    @NotNull
    private UserRole role;

    public void vincularEstabelecimento(Estabelecimento estabelecimento) {
        if (estabelecimento == null) {
            throw new IllegalArgumentException("Estabelecimento é obrigatório");
        }
        this.estabelecimento = estabelecimento;
    }

    public void vincularProfissionalPrincipal(Profissional profissional) {
        if (profissional == null) {
            throw new IllegalArgumentException("Profissional principal é obrigatório");
        }
        boolean mesmoEstabelecimento = estabelecimento == profissional.getEstabelecimento()
                || (estabelecimento != null && estabelecimento.getId() != null
                && estabelecimento.getId().equals(profissional.getEstabelecimento().getId()));
        if (estabelecimento != null && !mesmoEstabelecimento) {
            throw new IllegalArgumentException("Profissional deve pertencer ao estabelecimento do proprietário");
        }
        this.profissionalPrincipal = profissional;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role == UserRole.ADMIN ) return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),new SimpleGrantedAuthority("ROLE_USER"));
        else return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    @JsonIgnore
    public @Nullable String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
