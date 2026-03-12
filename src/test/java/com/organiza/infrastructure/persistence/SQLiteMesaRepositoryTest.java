package com.organiza.infrastructure.persistence;

import com.organiza.infrastructure.persistence.sqlite.SQLiteConnection;
import com.organiza.infrastructure.persistence.sqlite.SQLiteMesaRepository;

import com.organiza.domain.entity.Mesa;
import com.organiza.domain.enums.StatusMesa;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteMesaRepositoryTest {

    private static SQLiteConnection db;
    private SQLiteMesaRepository repository;

    @BeforeEach
    void setUp() {
        db = new SQLiteConnection("jdbc:sqlite::memory:");
        db.initializeDatabase();
        repository = new SQLiteMesaRepository(db);
    }

    @Test
    void deveSalvarMesa() {
        Mesa mesa = new Mesa(1);
        Mesa saved = repository.save(mesa);

        assertTrue(saved.getId() > 0);
        assertEquals(1, saved.getNumero());
        assertEquals(StatusMesa.LIVRE, saved.getStatus());
    }

    @Test
    void deveBuscarPorId() {
        Mesa mesa = repository.save(new Mesa(1));
        Optional<Mesa> found = repository.findById(mesa.getId());

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getNumero());
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrar() {
        Optional<Mesa> found = repository.findById(999);
        assertTrue(found.isEmpty());
    }

    @Test
    void deveBuscarPorNumero() {
        repository.save(new Mesa(5));
        Optional<Mesa> found = repository.findByNumero(5);

        assertTrue(found.isPresent());
        assertEquals(5, found.get().getNumero());
    }

    @Test
    void deveListarTodasOrdenadas() {
        repository.save(new Mesa(3));
        repository.save(new Mesa(1));
        repository.save(new Mesa(2));

        List<Mesa> mesas = repository.findAll();
        assertEquals(3, mesas.size());
        assertEquals(1, mesas.get(0).getNumero());
        assertEquals(2, mesas.get(1).getNumero());
        assertEquals(3, mesas.get(2).getNumero());
    }

    @Test
    void deveAtualizarStatus() {
        Mesa mesa = repository.save(new Mesa(1));
        repository.updateStatus(mesa.getId(), StatusMesa.OCUPADA);

        Mesa updated = repository.findById(mesa.getId()).orElseThrow();
        assertEquals(StatusMesa.OCUPADA, updated.getStatus());
    }

    @Test
    void deveRemoverMesa() {
        Mesa mesa = repository.save(new Mesa(1));
        repository.deleteById(mesa.getId());

        assertTrue(repository.findById(mesa.getId()).isEmpty());
    }
}
