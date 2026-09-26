package com.marquify.beta.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record HorariosLivresResponse(Long estabelecimentoId, Long profissionalId, Long servicoId,
                                     LocalDate data, List<LocalTime> horarios) {
}
