package com.marquify.beta.repository;

import com.marquify.beta.entity.DisponibilidadeProfissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DisponibilidadeProfissionalRepository extends JpaRepository<DisponibilidadeProfissional, Long> {
    List<DisponibilidadeProfissional> findAllByProfissionalIdOrderByDiaSemanaAsc(Long profissionalId);
    List<DisponibilidadeProfissional> findAllByProfissionalIdAndDiaSemana(Long profissionalId, DayOfWeek diaSemana);
    void deleteByProfissionalId(Long profissionalId);
}
