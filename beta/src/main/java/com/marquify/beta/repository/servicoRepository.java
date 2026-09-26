package com.marquify.beta.repository;

import com.marquify.beta.entity.Servicos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface servicoRepository extends JpaRepository<Servicos, Long> {
    java.util.List<Servicos> findAllByEstabelecimentoIdOrderByNomeAsc(Long estabelecimentoId);

    java.util.Optional<Servicos> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);

    java.util.Optional<Servicos> findByIdAndEstabelecimentoIdAndAtivoTrue(Long id, Long estabelecimentoId);

    java.util.Optional<Servicos> findByIdAndVendedorId(Long id, Long vendedorId);
}
