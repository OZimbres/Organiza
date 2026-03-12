package com.organiza.service;

import com.organiza.model.Produto;
import com.organiza.repository.ProdutoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Lógica de negócio para gestão de produtos do cardápio.
 */
public class ProdutoService {

    private final ProdutoRepository repo;

    public ProdutoService(ProdutoRepository repo) {
        this.repo = repo;
    }

    public Produto salvar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio.");
        }
        if (produto.getPreco() < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo.");
        }
        return repo.save(produto);
    }

    public Produto atualizar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio.");
        }
        if (produto.getPreco() < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo.");
        }
        return repo.update(produto);
    }

    public void deletar(int id) {
        repo.delete(id);
    }

    public List<Produto> listar() {
        return repo.findAll();
    }

    public Optional<Produto> buscar(int id) {
        return repo.findById(id);
    }
}
