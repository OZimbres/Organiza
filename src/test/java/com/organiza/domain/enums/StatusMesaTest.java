package com.organiza.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusMesaTest {

    @Test
    void deveRetornarLabelCorreto() {
        assertEquals("Livre", StatusMesa.LIVRE.getLabel());
        assertEquals("Ocupada", StatusMesa.OCUPADA.getLabel());
    }
}
