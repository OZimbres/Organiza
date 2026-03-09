package com.organiza.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MesaTest {

    @Test
    void deveCriarMesaComStatusLivre() {
        Mesa mesa = new Mesa(1);
        assertEquals(1, mesa.getNumero());
        assertEquals(StatusMesa.LIVRE, mesa.getStatus());
    }

    @Test
    void deveCriarMesaComTodosOsParametros() {
        Mesa mesa = new Mesa(1, 5, StatusMesa.OCUPADA);
        assertEquals(1, mesa.getId());
        assertEquals(5, mesa.getNumero());
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
    }

    @Test
    void deveAlterarStatus() {
        Mesa mesa = new Mesa(1);
        mesa.setStatus(StatusMesa.OCUPADA);
        assertEquals(StatusMesa.OCUPADA, mesa.getStatus());
    }

    @Test
    void deveRetornarToStringCorreto() {
        Mesa mesa = new Mesa(1, 3, StatusMesa.LIVRE);
        assertEquals("Mesa 3 - Livre", mesa.toString());
    }

    @Test
    void deveSerIguaisPeloId() {
        Mesa m1 = new Mesa(1, 1, StatusMesa.LIVRE);
        Mesa m2 = new Mesa(1, 2, StatusMesa.OCUPADA);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void deveSerDiferentesPeloId() {
        Mesa m1 = new Mesa(1, 1, StatusMesa.LIVRE);
        Mesa m2 = new Mesa(2, 1, StatusMesa.LIVRE);
        assertNotEquals(m1, m2);
    }
}
