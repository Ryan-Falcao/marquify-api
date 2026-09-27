package com.marquify.beta.repository;

import com.marquify.beta.entity.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {
    @Query("""
            select b from BloqueioAgenda b
            where b.profissional.id = :profissionalId
              and b.dataInicio <= :fim
              and (b.dataFim is null or b.dataFim >= :inicio)
            order by b.dataInicio asc, b.horaInicio asc
            """)
    List<BloqueioAgenda> buscarNoPeriodo(@Param("profissionalId") Long profissionalId,
                                         @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    Optional<BloqueioAgenda> findByIdAndProfissionalId(Long id, Long profissionalId);
}
