package com.marquify.beta.response;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class DashboardConsultaResponse {
    private DashboardConsultaResponse() {}
    public record Periodo(LocalDate inicio, LocalDate fim, String fusoHorario, ZonedDateTime referencia) {}
    public record Item(Long id, LocalDate data, LocalTime horaInicio, LocalTime horaFim, String status,
                       Long clienteId, String cliente, Long profissionalId, String profissional,
                       Long servicoId, String servico, BigDecimal valorCobrado, long duracaoMinutos) {}
    public record Pagina(Periodo periodo, List<Item> itens, int pagina, int tamanho, long totalItens,
                         long totalPaginas, String ordenarPor, String direcao) {}
    public record Indicadores(Periodo periodo, long totalAgendamentos, long previstos, long emAtendimento,
                              long finalizados, long cancelamentos, BigDecimal faturamentoRealizado,
                              BigDecimal faturamentoPrevisto, long reservasSemPreco) {}
}
