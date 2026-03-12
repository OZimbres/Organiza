package com.organiza.domain.entity;

import com.organiza.domain.enums.StatusPedido;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    void deveCriarPedidoComStatusPendente() {
        Pedido pedido = new Pedido(1, "João");
        assertEquals(1, pedido.getMesaId());
        assertEquals("João", pedido.getNomeCliente());
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());
        assertNotNull(pedido.getDataHora());
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void deveAdicionarItensECalcularTotal() {
        Pedido pedido = new Pedido(1, "Maria");
        pedido.addItem(new ItemPedido("Pão na chapa", 2, 5.00));
        pedido.addItem(new ItemPedido("Café", 1, 3.50));

        assertEquals(2, pedido.getItens().size());
        assertEquals("Pão na chapa", pedido.getItens().get(0).getProduto());
        assertEquals(2, pedido.getItens().get(0).getQuantidade());
        assertEquals(13.50, pedido.getTotal(), 0.001);
    }

    @Test
    void deveRetornarToStringCorreto() {
        Pedido pedido = new Pedido(1, 5, "Ana", StatusPedido.EM_PREPARO, LocalDateTime.now());
        assertEquals("Pedido #1 - Mesa 5 (Ana) - Em preparo", pedido.toString());
    }

    @Test
    void deveSerIguaisPeloId() {
        Pedido p1 = new Pedido(1, 1, "João", StatusPedido.PENDENTE, LocalDateTime.now());
        Pedido p2 = new Pedido(1, 2, "Maria", StatusPedido.PRONTO, LocalDateTime.now());
        assertEquals(p1, p2);
    }
}
