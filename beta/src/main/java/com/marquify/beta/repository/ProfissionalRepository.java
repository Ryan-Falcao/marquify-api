package com.marquify.beta.repository;

import com.marquify.beta.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    List<Profissional> findAllByEstabelecimentoIdAndAtivoTrue(Long estabelecimentoId);
    long countByEstabelecimentoIdAndAtivoTrue(Long estabelecimentoId);
    List<Profissional> findAllByEstabelecimentoIdOrderByNomeAsc(Long estabelecimentoId);
    java.util.Optional<Profissional> findByIdAndEstabelecimentoIdAndAtivoTrue(Long id, Long estabelecimentoId);
    java.util.Optional<Profissional> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);

    /** Serializa reservas do mesmo profissional para impedir duas confirmações no mesmo intervalo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profissional from Profissional profissional where profissional.id = :id "
            + "and profissional.estabelecimento.id = :estabelecimentoId and profissional.ativo = true")
    java.util.Optional<Profissional> findAtivoDoEstabelecimentoParaReserva(Long id, Long estabelecimentoId);
}
