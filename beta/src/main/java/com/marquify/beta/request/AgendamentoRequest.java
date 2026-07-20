package com.marquify.beta.request;


import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Cliente;
import com.marquify.beta.entity.Servicos;
import com.marquify.beta.entity.Vendedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoRequest {

    private LocalDate data;

    private LocalTime horaInicio;

    private Long clienteId;

    private Long vendedorId;

    private Long servicoId;

    private Long agendamentoId;
}
