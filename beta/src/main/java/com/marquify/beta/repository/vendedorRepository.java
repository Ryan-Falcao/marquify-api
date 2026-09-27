package com.marquify.beta.repository;

import com.marquify.beta.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface vendedorRepository extends JpaRepository<Vendedor, Long> {
    java.util.List<Vendedor> findAllByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    java.util.List<Vendedor> findAllByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEstabelecimentoId(Long estabelecimentoId);
}
