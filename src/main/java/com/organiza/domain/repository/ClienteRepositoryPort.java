package com.organiza.domain.repository;

import com.organiza.domain.entity.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) para operações de persistência de clientes.
 */
public interface ClienteRepositoryPort {

    Cliente save(Cliente cliente);

    Cliente update(Cliente cliente);

    void delete(int id);

    List<Cliente> findAll();

    Optional<Cliente> findById(int id);
}
