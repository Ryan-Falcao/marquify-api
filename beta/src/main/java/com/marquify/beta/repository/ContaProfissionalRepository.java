package com.marquify.beta.repository;
import com.marquify.beta.entity.ContaProfissional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ContaProfissionalRepository extends JpaRepository<ContaProfissional, Long> {
    Optional<ContaProfissional> findByProfissionalId(Long profissionalId);
    Optional<ContaProfissional> findByConviteToken(String conviteToken);
    List<ContaProfissional> findAllByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndProfissionalIdNot(String email, Long profissionalId);
}
