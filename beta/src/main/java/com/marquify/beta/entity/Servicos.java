package com.marquify.beta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servicos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    private Double preco;

    private LocalTime tempo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Referência temporária para compatibilidade com as rotas legadas baseadas no vendedor.
     * O estabelecimento é o dono canônico do catálogo.
     */
    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Vendedor vendedor;

    @ManyToMany
    @JoinTable(name = "servico_profissionais",
            joinColumns = @JoinColumn(name = "servico_id"),
            inverseJoinColumns = @JoinColumn(name = "profissional_id"))
    private Set<Profissional> profissionais = new LinkedHashSet<>();

    public void definirProfissionais(Set<Profissional> profissionais) {
        if (profissionais == null || profissionais.isEmpty()) {
            throw new IllegalArgumentException("Serviço deve ter ao menos um profissional");
        }
        this.profissionais = new LinkedHashSet<>(profissionais);
    }

    public boolean executadoPor(Long profissionalId) {
        return profissionais.stream().anyMatch(profissional -> profissional.getId().equals(profissionalId));
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

}
