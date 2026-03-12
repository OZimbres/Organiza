package com.organiza.application.usecase;

import com.organiza.domain.entity.Cliente;
import com.organiza.infrastructure.persistence.sqlite.SQLiteClienteRepository;

import java.util.List;
import java.util.Optional;

/**
 * Lógica de negócio para gestão de clientes.
 */
public class ClienteService {

    private final SQLiteClienteRepository repo;

    public ClienteService(SQLiteClienteRepository repo) {
        this.repo = repo;
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do cliente não pode ser vazio.");
        }
        return repo.save(cliente);
    }

    public Cliente atualizar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do cliente não pode ser vazio.");
        }
        return repo.update(cliente);
    }

    public void deletar(int id) {
        repo.delete(id);
    }

    public List<Cliente> listar() {
        return repo.findAll();
    }

    public Optional<Cliente> buscar(int id) {
        return repo.findById(id);
    }
}
