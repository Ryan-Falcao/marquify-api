package com.marquify.beta.repository;

import com.marquify.beta.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface clienteRepository extends JpaRepository<Cliente, Long> {
    java.util.List<Cliente> findAllByEmail(String email);
    boolean existsByEmail(String email);

}
