package com.marquify.beta.repository;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface agendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByVendedorId(Long vendedorId);
}
