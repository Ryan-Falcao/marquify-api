package com.marquify.beta.repository;

import com.marquify.beta.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface clienteRepository extends JpaRepository<Cliente, Long> {
}
