package com.marquify.beta.request;

import com.marquify.beta.entity.Vendedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class ServicoRequest {

    private String nome;

    private String descricao;

    private Double preco;

    private LocalTime tempo;

    private Long vendedorId;

    private Long servicoId;
}
