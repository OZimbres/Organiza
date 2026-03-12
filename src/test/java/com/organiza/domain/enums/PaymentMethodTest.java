package com.organiza.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {

    @Test
    void deveRetornarLabelCorreto() {
        assertEquals("Dinheiro", PaymentMethod.DINHEIRO.getLabel());
        assertEquals("Cartão de Crédito", PaymentMethod.CARTAO_CREDITO.getLabel());
        assertEquals("Cartão de Débito", PaymentMethod.CARTAO_DEBITO.getLabel());
        assertEquals("Pix", PaymentMethod.PIX.getLabel());
    }

    @Test
    void deveConterTodosOsMetodos() {
        assertEquals(4, PaymentMethod.values().length);
    }
}
