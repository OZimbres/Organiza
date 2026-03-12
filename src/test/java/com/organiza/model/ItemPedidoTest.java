package com.organiza.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCriarItemComProdutoQuantidadeEPreco() {
        ItemPedido item = new ItemPedido("Pão na chapa", 2, 5.50);
        assertEquals("Pão na chapa", item.getProduto());
        assertEquals(2, item.getQuantidade());
        assertEquals(5.50, item.getPreco());
        assertEquals(11.00, item.getSubtotal());
    }

    @Test
    void deveCriarItemComTodosOsParametros() {
        ItemPedido item = new ItemPedido(1, 10, "Café", 3, 3.00);
        assertEquals(1, item.getId());
        assertEquals(10, item.getPedidoId());
        assertEquals("Café", item.getProduto());
        assertEquals(3, item.getQuantidade());
        assertEquals(3.00, item.getPreco());
        assertEquals(9.00, item.getSubtotal());
    }

    @Test
    void deveRetornarToStringCorreto() {
        ItemPedido item = new ItemPedido("Suco de laranja", 1, 0.0);
        assertEquals("1x Suco de laranja", item.toString());
    }
}
