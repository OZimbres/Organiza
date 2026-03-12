package com.organiza.infrastructure.persistence;

import com.organiza.infrastructure.persistence.sqlite.SQLiteConnection;
import com.organiza.infrastructure.persistence.sqlite.SQLiteClienteRepository;


import com.organiza.domain.entity.Cliente;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteClienteRepositoryTest {

    private SQLiteConnection db;
    private SQLiteClienteRepository repo;

    @BeforeEach
    void setUp() {
        db = new SQLiteConnection("jdbc:sqlite::memory:");
        db.initializeDatabase();
        repo = new SQLiteClienteRepository(db);
    }

    @Test
    void deveSalvarERetornarCliente() {
        Cliente c = repo.save(new Cliente("Maria", "(11) 99999-0000"));
        assertTrue(c.getId() > 0);
        assertEquals("Maria", c.getNome());
    }

    @Test
    void deveBuscarClientePorId() {
        Cliente saved = repo.save(new Cliente("Carlos", null));
        Optional<Cliente> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Carlos", found.get().getNome());
    }

    @Test
    void deveListarTodosClientes() {
        repo.save(new Cliente("Ana", null));
        repo.save(new Cliente("Bruno", "99"));
        List<Cliente> list = repo.findAll();
        assertEquals(2, list.size());
    }

    @Test
    void deveAtualizarCliente() {
        Cliente c = repo.save(new Cliente("Velho", null));
        c.setNome("Novo");
        c.setTelefone("123");
        repo.update(c);
        Optional<Cliente> updated = repo.findById(c.getId());
        assertEquals("Novo", updated.get().getNome());
        assertEquals("123", updated.get().getTelefone());
    }

    @Test
    void deveDeletarCliente() {
        Cliente c = repo.save(new Cliente("Temp", null));
        repo.delete(c.getId());
        assertTrue(repo.findById(c.getId()).isEmpty());
    }
}
