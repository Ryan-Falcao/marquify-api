package com.marquify.beta.repository;

import com.marquify.beta.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface vendedorRepository extends JpaRepository<Vendedor, Long> {
}
