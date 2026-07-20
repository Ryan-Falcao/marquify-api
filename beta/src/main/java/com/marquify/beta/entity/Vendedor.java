package com.marquify.beta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "vendedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nome;

    @NotNull
    private String email;

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
    @Column(name = "dia")
    private Set<DiasAbertos> diasAbertos;
}
