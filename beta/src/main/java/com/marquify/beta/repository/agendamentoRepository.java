package com.marquify.beta.repository;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
import com.marquify.beta.entity.Status;
import java.time.LocalTime;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface agendamentoRepository extends JpaRepository<Agendamento, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id")
    java.util.Optional<Agendamento> findParaAlteracao(Long id);
    java.util.Optional<Agendamento> findByIdAndClienteId(Long id, Long clienteId);
    java.util.Optional<Agendamento> findByIdAndVendedorId(Long id, Long vendedorId);
    List<Agendamento> findByVendedorId(Long vendedorId);
    List<Agendamento> findAllByVendedorIdAndDataBetweenOrderByDataAscHoraInicioAsc(Long vendedorId, LocalDate inicio, LocalDate fim);
    List<Agendamento> findAllByVendedorIdAndDataAndStatusOrderByHoraInicioAsc(Long vendedorId, LocalDate data, Status status);
    List<Agendamento> findAllByProfissionalIdAndDataAndStatus(Long profissionalId, LocalDate data, Status status);
    List<Agendamento> findAllByClienteIdOrderByDataDescHoraInicioDesc(Long clienteId);

    @Query("""
            select coalesce(sum(a.valorCobrado), 0) from Agendamento a
            where a.vendedor.id = :vendedorId and a.status = :status
              and (a.data < :hoje or (a.data = :hoje and a.horaFim <= :agora))
            """)
    BigDecimal totalFinalizado(@Param("vendedorId") Long vendedorId, @Param("status") Status status,
                               @Param("hoje") LocalDate hoje, @Param("agora") LocalTime agora);
}
