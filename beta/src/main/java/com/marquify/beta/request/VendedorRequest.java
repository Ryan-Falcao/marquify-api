package com.marquify.beta.request;

import com.marquify.beta.entity.DiasAbertos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendedorRequest {

    private Long vendedor_id;

    private String newNome;

    private String newNomeLoja;

    private Set<DiasAbertos> newDiasAbertos;

    private LocalTime newHoraAbertura;

    private LocalTime newHoraFechamento;
}
