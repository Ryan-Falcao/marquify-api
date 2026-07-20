package com.marquify.beta.repository;

import com.marquify.beta.entity.Servicos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface servicoRepository extends JpaRepository<Servicos, Long> {
}
