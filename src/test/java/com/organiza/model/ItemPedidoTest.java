package com.organiza.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCriarItemComProdutoEQuantidade() {
        ItemPedido item = new ItemPedido("Pão na chapa", 2);
        assertEquals("Pão na chapa", item.getProduto());
        assertEquals(2, item.getQuantidade());
    }

    @Test
    void deveCriarItemComTodosOsParametros() {
        ItemPedido item = new ItemPedido(1, 10, "Café", 3);
        assertEquals(1, item.getId());
        assertEquals(10, item.getPedidoId());
        assertEquals("Café", item.getProduto());
        assertEquals(3, item.getQuantidade());
    }

    @Test
    void deveRetornarToStringCorreto() {
        ItemPedido item = new ItemPedido("Suco de laranja", 1);
        assertEquals("1x Suco de laranja", item.toString());
    }
}
