package com.marquify.beta.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "interesses_comerciais")
@Getter
@NoArgsConstructor
public class InteresseComercial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String estabelecimento;
    private String email;
    private String whatsapp;
    private String segmento;
    private Instant criadoEm;

    public InteresseComercial(String nome, String estabelecimento, String email, String whatsapp, String segmento) {
        this.nome = nome.trim();
        this.estabelecimento = estabelecimento.trim();
        this.email = email.trim().toLowerCase();
        this.whatsapp = whatsapp.trim();
        this.segmento = segmento.trim();
    }

    @PrePersist
    void registrarCriacao() { criadoEm = Instant.now(); }
}
