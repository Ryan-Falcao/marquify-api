package com.marquify.beta.response;

import java.util.List;
import java.math.BigDecimal;

public record DashboardResponse(
        String nome,
        long servicosAtivos,
        long profissionaisAtivos,
        long agendamentosHoje,
        BigDecimal totalFaturado,
        List<AgendamentoResponse> proximosAgendamentos
) {}
