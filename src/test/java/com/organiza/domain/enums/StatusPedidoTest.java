package com.organiza.domain.enums;

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

    @Test
    void deveRetornarProximoStatusCorreto() {
        assertEquals(StatusPedido.EM_PREPARO, StatusPedido.PENDENTE.next());
        assertEquals(StatusPedido.PRONTO, StatusPedido.EM_PREPARO.next());
        assertEquals(StatusPedido.ENTREGUE, StatusPedido.PRONTO.next());
        assertEquals(StatusPedido.PAGO, StatusPedido.ENTREGUE.next());
    }

    @Test
    void deveLancarExcecaoAoAvancarPago() {
        assertThrows(IllegalStateException.class, () -> StatusPedido.PAGO.next());
    }

    @Test
    void deveValidarTransicoesPermitidas() {
        assertTrue(StatusPedido.PENDENTE.canTransitionTo(StatusPedido.EM_PREPARO));
        assertFalse(StatusPedido.PENDENTE.canTransitionTo(StatusPedido.PAGO));
        assertFalse(StatusPedido.PAGO.canTransitionTo(StatusPedido.PENDENTE));
        assertTrue(StatusPedido.ENTREGUE.canTransitionTo(StatusPedido.PAGO));
    }

    @Test
    void deveRetornarTransicoesVaziasParaPago() {
        assertTrue(StatusPedido.PAGO.getAllowedTransitions().isEmpty());
    }
}
