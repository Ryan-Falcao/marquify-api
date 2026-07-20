package com.marquify.beta.repository;

import com.marquify.beta.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface vendedorRepository extends JpaRepository<Vendedor, Long> {
    UserDetails findByEmail(String email);
}
