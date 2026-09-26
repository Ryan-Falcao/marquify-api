package com.marquify.beta.repository;

import com.marquify.beta.entity.Agendamento;
import com.marquify.beta.entity.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
import com.marquify.beta.entity.Status;

public interface agendamentoRepository extends JpaRepository<Agendamento, Long> {
    java.util.Optional<Agendamento> findByIdAndClienteId(Long id, Long clienteId);
    java.util.Optional<Agendamento> findByIdAndVendedorId(Long id, Long vendedorId);
    List<Agendamento> findByVendedorId(Long vendedorId);
    List<Agendamento> findAllByProfissionalIdAndDataAndStatus(Long profissionalId, LocalDate data, Status status);
}
