package com.marquify.beta.response;

import com.marquify.beta.entity.DiasAbertos;
import com.marquify.beta.entity.Vendedor;
import java.time.LocalTime;
import java.util.Set;

public record VendedorResponse(Long id, String nome, String email, String nomeLoja,
                               LocalTime horaAbertura, LocalTime horaFechamento, Set<DiasAbertos> diasAbertos) {
    public static VendedorResponse from(Vendedor vendedor) {
        return new VendedorResponse(vendedor.getId(), vendedor.getNome(), vendedor.getEmail(),
                vendedor.getNomeLoja(), vendedor.getHoraAbertura(), vendedor.getHoraFechamento(),
                Set.copyOf(vendedor.getDiasAbertos()));
    }
}
