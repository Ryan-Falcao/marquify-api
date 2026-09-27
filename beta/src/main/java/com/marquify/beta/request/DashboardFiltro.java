package com.marquify.beta.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record DashboardFiltro(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
        Long profissionalId, Long clienteId, String status,
        Integer pagina, Integer tamanho, String ordenarPor, String direcao) {}
