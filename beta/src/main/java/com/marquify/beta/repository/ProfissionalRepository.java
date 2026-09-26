package com.marquify.beta.repository;

import com.marquify.beta.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    List<Profissional> findAllByEstabelecimentoIdAndAtivoTrue(Long estabelecimentoId);
    List<Profissional> findAllByEstabelecimentoIdOrderByNomeAsc(Long estabelecimentoId);
    java.util.Optional<Profissional> findByIdAndEstabelecimentoIdAndAtivoTrue(Long id, Long estabelecimentoId);
    java.util.Optional<Profissional> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
