package com.organiza.service;

import com.organiza.database.DatabaseConnection;
import com.organiza.model.*;
import com.organiza.repository.MesaRepository;
import com.organiza.repository.PedidoRepository;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    private PedidoService service;
    private MesaRepository mesaRepository;

    @BeforeEach
    void setUp() {
        DatabaseConnection db = new DatabaseConnection("jdbc:sqlite::memory:");
        db.initializeDatabase();
        mesaRepository = new MesaRepository(db);
        PedidoRepository pedidoRepository = new PedidoRepository(db);
        service = new PedidoService(mesaRepository, pedidoRepository);
    }

    @Test
    void deveCriarMesas() {
        service.criarMesas(5);
        List<Mesa> mesas = service.listarMesas();
        assertEquals(5, mesas.size());
        assertEquals(1, mesas.get(0).getNumero());
        assertEquals(5, mesas.get(4).getNumero());
    }

    @Test
    void naoDeveDuplicarMesas() {
        service.criarMesas(3);
        service.criarMesas(5);
        List<Mesa> mesas = service.listarMesas();
        assertEquals(5, mesas.size());
    }

    @Test
    void deveCriarPedido() {
        service.criarMesas(1);
        Mesa mesa = service.listarMesas().getFirst();

        List<ItemPedido> itens = List.of(
                new ItemPedido("Pão na chapa", 2),
                new ItemPedido("Café", 1)
        );

        Pedido pedido = service.criarPedido(mesa.getId(), itens);

        assertNotNull(pedido);
        assertTrue(pedido.getId() > 0);
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());
        assertEquals(2, pedido.getItens().size());

        // Mesa deve ficar ocupada
        Mesa mesaAtualizada = service.buscarMesa(mesa.getId()).orElseThrow();
        assertEquals(StatusMesa.OCUPADA, mesaAtualizada.getStatus());
    }

    @Test
    void deveLancarExcecaoParaMesaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criarPedido(999, List.of(new ItemPedido("Café", 1))));
    }

    @Test
    void deveLancarExcecaoParaPedidoSemItens() {
        service.criarMesas(1);
        Mesa mesa = service.listarMesas().getFirst();

        assertThrows(IllegalStateException.class,
                () -> service.criarPedido(mesa.getId(), List.of()));
    }

    @Test
    void deveAvancarStatusCompleto() {
        service.criarMesas(1);
        Mesa mesa = service.listarMesas().getFirst();
        Pedido pedido = service.criarPedido(mesa.getId(),
                List.of(new ItemPedido("Café", 1)));

        // PENDENTE → EM_PREPARO
        Pedido p1 = service.avancarStatus(pedido.getId());
        assertEquals(StatusPedido.EM_PREPARO, p1.getStatus());

        // EM_PREPARO → PRONTO
        Pedido p2 = service.avancarStatus(pedido.getId());
        assertEquals(StatusPedido.PRONTO, p2.getStatus());

        // PRONTO → ENTREGUE
        Pedido p3 = service.avancarStatus(pedido.getId());
        assertEquals(StatusPedido.ENTREGUE, p3.getStatus());

        // ENTREGUE → PAGO
        Pedido p4 = service.avancarStatus(pedido.getId());
        assertEquals(StatusPedido.PAGO, p4.getStatus());

        // Mesa deve voltar a ficar livre
        Mesa mesaAtualizada = service.buscarMesa(mesa.getId()).orElseThrow();
        assertEquals(StatusMesa.LIVRE, mesaAtualizada.getStatus());
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoPago() {
        service.criarMesas(1);
        Mesa mesa = service.listarMesas().getFirst();
        Pedido pedido = service.criarPedido(mesa.getId(),
                List.of(new ItemPedido("Café", 1)));

        // Avança até PAGO
        service.avancarStatus(pedido.getId());
        service.avancarStatus(pedido.getId());
        service.avancarStatus(pedido.getId());
        service.avancarStatus(pedido.getId());

        assertThrows(IllegalStateException.class,
                () -> service.avancarStatus(pedido.getId()));
    }

    @Test
    void naoDeveLiberarMesaComPedidoPendente() {
        service.criarMesas(1);
        Mesa mesa = service.listarMesas().getFirst();

        // Cria dois pedidos
        Pedido p1 = service.criarPedido(mesa.getId(),
                List.of(new ItemPedido("Café", 1)));
        Pedido p2 = service.criarPedido(mesa.getId(),
                List.of(new ItemPedido("Suco", 1)));

        // Paga apenas o primeiro
        service.avancarStatus(p1.getId()); // EM_PREPARO
        service.avancarStatus(p1.getId()); // PRONTO
        service.avancarStatus(p1.getId()); // ENTREGUE
        service.avancarStatus(p1.getId()); // PAGO

        // Mesa ainda deve estar ocupada porque p2 está pendente
        Mesa mesaAtualizada = service.buscarMesa(mesa.getId()).orElseThrow();
        assertEquals(StatusMesa.OCUPADA, mesaAtualizada.getStatus());
    }

    @Test
    void deveListarPedidosCozinha() {
        service.criarMesas(2);
        List<Mesa> mesas = service.listarMesas();

        service.criarPedido(mesas.get(0).getId(),
                List.of(new ItemPedido("Café", 1)));
        Pedido p2 = service.criarPedido(mesas.get(1).getId(),
                List.of(new ItemPedido("Suco", 1)));

        // Avança p2 para EM_PREPARO
        service.avancarStatus(p2.getId());

        List<Pedido> cozinha = service.listarPedidosCozinha();
        assertEquals(2, cozinha.size());
    }

    @Test
    void deveListarPedidosAtivos() {
        service.criarMesas(1);
        Mesa mesa = service.listarMesas().getFirst();
        Pedido pedido = service.criarPedido(mesa.getId(),
                List.of(new ItemPedido("Café", 1)));

        List<Pedido> ativos = service.listarPedidosAtivos();
        assertEquals(1, ativos.size());

        // Paga o pedido
        service.avancarStatus(pedido.getId());
        service.avancarStatus(pedido.getId());
        service.avancarStatus(pedido.getId());
        service.avancarStatus(pedido.getId());

        ativos = service.listarPedidosAtivos();
        assertTrue(ativos.isEmpty());
    }
}
