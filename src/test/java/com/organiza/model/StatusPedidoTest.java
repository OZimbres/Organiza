package com.organiza.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusPedidoTest {

    @Test
    void deveRetornarLabelCorreto() {
        assertEquals("Pendente", StatusPedido.PENDENTE.getLabel());
        assertEquals("Em preparo", StatusPedido.EM_PREPARO.getLabel());
        assertEquals("Pronto", StatusPedido.PRONTO.getLabel());
        assertEquals("Entregue", StatusPedido.ENTREGUE.getLabel());
        assertEquals("Pago", StatusPedido.PAGO.getLabel());
    }
}
