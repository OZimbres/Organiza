package com.organiza.repository;

import com.organiza.database.DatabaseConnection;
import com.organiza.model.Produto;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoRepositoryTest {

    private DatabaseConnection db;
    private ProdutoRepository repo;

    @BeforeEach
    void setUp() {
        db = new DatabaseConnection("jdbc:sqlite::memory:");
        db.initializeDatabase();
        repo = new ProdutoRepository(db);
    }

    @Test
    void deveSalvarERetornarProduto() {
        Produto p = repo.save(new Produto("Pão na chapa", 5.50, "Salgados"));
        assertTrue(p.getId() > 0);
        assertEquals("Pão na chapa", p.getNome());
        assertEquals(5.50, p.getPreco(), 0.001);
    }

    @Test
    void deveBuscarProdutoPorId() {
        Produto saved = repo.save(new Produto("Café", 3.00, "Bebidas"));
        Optional<Produto> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Café", found.get().getNome());
        assertEquals("Bebidas", found.get().getCategoria());
    }

    @Test
    void deveListarTodosProdutos() {
        repo.save(new Produto("Coxinha", 6.00, "Salgados"));
        repo.save(new Produto("Suco", 7.00, "Bebidas"));
        List<Produto> list = repo.findAll();
        assertEquals(2, list.size());
    }

    @Test
    void deveAtualizarProduto() {
        Produto p = repo.save(new Produto("Velho", 1.00, null));
        p.setNome("Novo");
        p.setPreco(9.99);
        p.setCategoria("Doces");
        repo.update(p);
        Optional<Produto> updated = repo.findById(p.getId());
        assertEquals("Novo", updated.get().getNome());
        assertEquals(9.99, updated.get().getPreco(), 0.001);
        assertEquals("Doces", updated.get().getCategoria());
    }

    @Test
    void deveDeletarProduto() {
        Produto p = repo.save(new Produto("Temp", 0, null));
        repo.delete(p.getId());
        assertTrue(repo.findById(p.getId()).isEmpty());
    }
}
