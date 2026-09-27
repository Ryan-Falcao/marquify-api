package com.marquify.beta.response;

import com.marquify.beta.entity.Cliente;

public record ClienteResponse(Long id, String nome, String email, String numero) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getNumero());
    }
}
