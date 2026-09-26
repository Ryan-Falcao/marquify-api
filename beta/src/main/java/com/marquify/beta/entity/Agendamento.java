package com.marquify.beta.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    private Status status;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Vendedor vendedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servicos servico;

    public void vincularProfissional(Profissional profissional) {
        if (profissional == null) {
            throw new IllegalArgumentException("Profissional é obrigatório");
        }
        boolean mesmoEstabelecimento = estabelecimento == profissional.getEstabelecimento()
                || (estabelecimento != null && estabelecimento.getId() != null
                && estabelecimento.getId().equals(profissional.getEstabelecimento().getId()));
        if (estabelecimento != null && !mesmoEstabelecimento) {
            throw new IllegalArgumentException("Profissional deve pertencer ao estabelecimento do agendamento");
        }
        this.profissional = profissional;
    }

    public void vincularEstabelecimento(Estabelecimento estabelecimento) {
        if (estabelecimento == null) {
            throw new IllegalArgumentException("Estabelecimento é obrigatório");
        }
        boolean mesmoEstabelecimento = profissional == null
                || profissional.getEstabelecimento() == estabelecimento
                || (estabelecimento.getId() != null && estabelecimento.getId().equals(profissional.getEstabelecimento().getId()));
        if (!mesmoEstabelecimento) {
            throw new IllegalArgumentException("Estabelecimento deve ser o mesmo do profissional");
        }
        this.estabelecimento = estabelecimento;
    }

}
