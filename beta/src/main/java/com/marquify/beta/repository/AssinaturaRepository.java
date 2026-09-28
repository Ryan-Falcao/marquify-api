package com.marquify.beta.repository;

import com.marquify.beta.entity.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
    Optional<Assinatura> findByEstabelecimentoId(Long estabelecimentoId);
    Optional<Assinatura> findByMercadoPagoAssinaturaId(String mercadoPagoAssinaturaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Assinatura a where a.estabelecimento.id = :estabelecimentoId")
    Optional<Assinatura> findByEstabelecimentoIdParaAlteracao(Long estabelecimentoId);
}
