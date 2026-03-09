package com.organiza.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    void deveCriarPedidoComStatusPendente() {
        Pedido pedido = new Pedido(1);
        assertEquals(1, pedido.getMesaId());
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());
        assertNotNull(pedido.getDataHora());
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void deveAdicionarItens() {
        Pedido pedido = new Pedido(1);
        pedido.addItem(new ItemPedido("Pão na chapa", 2));
        pedido.addItem(new ItemPedido("Café", 1));

        assertEquals(2, pedido.getItens().size());
        assertEquals("Pão na chapa", pedido.getItens().get(0).getProduto());
        assertEquals(2, pedido.getItens().get(0).getQuantidade());
    }

    @Test
    void deveRetornarToStringCorreto() {
        Pedido pedido = new Pedido(1, 5, StatusPedido.EM_PREPARO, LocalDateTime.now());
        assertEquals("Pedido #1 - Mesa 5 - Em preparo", pedido.toString());
    }

    @Test
    void deveSerIguaisPeloId() {
        Pedido p1 = new Pedido(1, 1, StatusPedido.PENDENTE, LocalDateTime.now());
        Pedido p2 = new Pedido(1, 2, StatusPedido.PRONTO, LocalDateTime.now());
        assertEquals(p1, p2);
    }
}
