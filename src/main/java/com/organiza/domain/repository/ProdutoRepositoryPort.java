package com.organiza.domain.repository;

import com.organiza.domain.entity.Produto;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) para operações de persistência de produtos.
 */
public interface ProdutoRepositoryPort {

    Produto save(Produto produto);

    Produto update(Produto produto);

    void delete(int id);

    List<Produto> findAll();

    Optional<Produto> findById(int id);
}
