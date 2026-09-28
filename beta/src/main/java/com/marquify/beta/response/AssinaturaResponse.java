package com.marquify.beta.response;

import com.marquify.beta.entity.PlanoAssinatura;
import com.marquify.beta.entity.StatusAssinatura;

import java.time.Instant;

public record AssinaturaResponse(
        PlanoAssinatura plano,
        StatusAssinatura status,
        Instant testeGratisAte,
        long diasRestantesTeste,
        LimitesPlano limites,
        UsoPlano uso
) {
    public record LimitesPlano(int servicos, int profissionais, int agendamentosMensais) {}
    public record UsoPlano(long servicos, long profissionais, long agendamentosNoMes) {}
}
