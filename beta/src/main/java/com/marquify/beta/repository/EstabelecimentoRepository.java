package com.marquify.beta.repository;

import com.marquify.beta.entity.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
    java.util.Optional<Estabelecimento> findByIdAndAtivoTrue(Long id);
    java.util.Optional<Estabelecimento> findByCodigoPublicoAndAtivoTrue(String codigoPublico);
    java.util.Optional<Estabelecimento> findBySlugPublicoAndAtivoTrue(String slugPublico);
    boolean existsBySlugPublicoAndIdNot(String slugPublico, Long id);
}
