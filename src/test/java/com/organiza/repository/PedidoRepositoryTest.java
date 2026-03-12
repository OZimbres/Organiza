package com.organiza.repository;

import com.organiza.database.DatabaseConnection;
import com.organiza.model.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PedidoRepositoryTest {

    private DatabaseConnection db;
    private MesaRepository mesaRepository;
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        db = new DatabaseConnection("jdbc:sqlite::memory:");
        db.initializeDatabase();
        mesaRepository = new MesaRepository(db);
        pedidoRepository = new PedidoRepository(db);

        // Cria uma mesa para os testes
        mesaRepository.save(new Mesa(1));
    }

    @Test
    void deveSalvarPedidoComItens() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();
        Pedido pedido = new Pedido(mesa.getId(), "João");
        pedido.addItem(new ItemPedido("Pão na chapa", 2, 5.00));
        pedido.addItem(new ItemPedido("Café", 1, 3.50));

        Pedido saved = pedidoRepository.save(pedido);

        assertTrue(saved.getId() > 0);
        assertEquals("João", saved.getNomeCliente());
        assertEquals(2, saved.getItens().size());
        assertTrue(saved.getItens().get(0).getId() > 0);
        assertEquals(saved.getId(), saved.getItens().get(0).getPedidoId());
        assertEquals(5.00, saved.getItens().get(0).getPreco());
    }

    @Test
    void deveBuscarPorId() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();
        Pedido pedido = new Pedido(mesa.getId(), "Ana");
        pedido.addItem(new ItemPedido("Café", 1, 3.50));
        Pedido saved = pedidoRepository.save(pedido);

        Optional<Pedido> found = pedidoRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Ana", found.get().getNomeCliente());
        assertEquals(StatusPedido.PENDENTE, found.get().getStatus());
        assertEquals(1, found.get().getItens().size());
        assertEquals("Café", found.get().getItens().get(0).getProduto());
        assertEquals(3.50, found.get().getItens().get(0).getPreco());
    }

    @Test
    void deveListarPorMesa() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();

        Pedido p1 = new Pedido(mesa.getId(), "Maria");
        p1.addItem(new ItemPedido("Café", 1, 0.0));
        pedidoRepository.save(p1);

        Pedido p2 = new Pedido(mesa.getId(), "Carlos");
        p2.addItem(new ItemPedido("Suco", 2, 0.0));
        pedidoRepository.save(p2);

        List<Pedido> pedidos = pedidoRepository.findByMesaId(mesa.getId());
        assertEquals(2, pedidos.size());
    }

    @Test
    void deveListarPorStatus() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();
        Pedido pedido = new Pedido(mesa.getId(), "Luís");
        pedido.addItem(new ItemPedido("Café", 1, 0.0));
        pedidoRepository.save(pedido);

        List<Pedido> pendentes = pedidoRepository.findByStatus(StatusPedido.PENDENTE);
        assertEquals(1, pendentes.size());

        List<Pedido> prontos = pedidoRepository.findByStatus(StatusPedido.PRONTO);
        assertTrue(prontos.isEmpty());
    }

    @Test
    void deveAtualizarStatus() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();
        Pedido pedido = new Pedido(mesa.getId(), "Sara");
        pedido.addItem(new ItemPedido("Café", 1, 0.0));
        Pedido saved = pedidoRepository.save(pedido);

        pedidoRepository.updateStatus(saved.getId(), StatusPedido.EM_PREPARO);

        Pedido updated = pedidoRepository.findById(saved.getId()).orElseThrow();
        assertEquals(StatusPedido.EM_PREPARO, updated.getStatus());
    }

    @Test
    void deveListarAtivos() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();

        Pedido p1 = new Pedido(mesa.getId(), "Rita");
        p1.addItem(new ItemPedido("Café", 1, 0.0));
        Pedido saved1 = pedidoRepository.save(p1);

        Pedido p2 = new Pedido(mesa.getId(), "Tiago");
        p2.addItem(new ItemPedido("Suco", 1, 0.0));
        pedidoRepository.save(p2);

        // Marca um como pago
        pedidoRepository.updateStatus(saved1.getId(), StatusPedido.PAGO);

        List<Pedido> ativos = pedidoRepository.findAllActive();
        assertEquals(1, ativos.size());
    }

    @Test
    void deveRemoverPedidoComItens() {
        Mesa mesa = mesaRepository.findByNumero(1).orElseThrow();
        Pedido pedido = new Pedido(mesa.getId(), "Vera");
        pedido.addItem(new ItemPedido("Café", 1, 0.0));
        Pedido saved = pedidoRepository.save(pedido);

        pedidoRepository.deleteById(saved.getId());

        assertTrue(pedidoRepository.findById(saved.getId()).isEmpty());
        assertTrue(pedidoRepository.findItensByPedidoId(saved.getId()).isEmpty());
    }
}
