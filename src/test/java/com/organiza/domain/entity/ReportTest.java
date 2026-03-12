package com.organiza.domain.entity;

import com.organiza.domain.enums.StatusPedido;

import org.junit.jupiter.api.Test;

import java.util.DoubleSummaryStatistics;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

    @Test
    void deveCriarReportComDadosCompletos() {
        Map<StatusPedido, Long> porStatus = Map.of(
                StatusPedido.PENDENTE, 3L,
                StatusPedido.PAGO, 5L
        );
        Map<String, Integer> topItens = Map.of("Café", 10, "Pão", 8);
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        stats.accept(25.0);
        stats.accept(30.0);

        Report report = new Report(150.0, porStatus, topItens, stats, 3);

        assertEquals(150.0, report.getReceitaTotal(), 0.001);
        assertEquals(3, report.getPedidosAtivos());
        assertEquals(8, report.getTotalPedidos());
        assertEquals(2, report.getItensMaisVendidos().size());
        assertEquals(10, report.getItensMaisVendidos().get("Café"));
        assertNotNull(report.getEstatisticas());
        assertEquals(2, report.getPedidosPorStatus().size());
    }

    @Test
    void deveCriarReportComDadosNulos() {
        Report report = new Report(0.0, null, null, null, 0);

        assertEquals(0.0, report.getReceitaTotal(), 0.001);
        assertEquals(0, report.getPedidosAtivos());
        assertEquals(0, report.getTotalPedidos());
        assertTrue(report.getItensMaisVendidos().isEmpty());
        assertTrue(report.getPedidosPorStatus().isEmpty());
        assertNull(report.getEstatisticas());
    }

    @Test
    void deveRetornarTotalPedidosCorreto() {
        Map<StatusPedido, Long> porStatus = Map.of(
                StatusPedido.PENDENTE, 2L,
                StatusPedido.EM_PREPARO, 3L,
                StatusPedido.PRONTO, 1L,
                StatusPedido.PAGO, 10L
        );

        Report report = new Report(500.0, porStatus, Map.of(), null, 6);

        assertEquals(16, report.getTotalPedidos());
    }

    @Test
    void deveSerIgualComMesmosDados() {
        Map<StatusPedido, Long> porStatus = Map.of(StatusPedido.PAGO, 5L);
        Map<String, Integer> topItens = Map.of("Café", 10);

        Report r1 = new Report(100.0, porStatus, topItens, null, 2);
        Report r2 = new Report(100.0, porStatus, topItens, null, 2);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveRetornarToStringComResumo() {
        Report report = new Report(250.50, Map.of(StatusPedido.PAGO, 5L), Map.of(), null, 3);

        String str = report.toString();
        assertTrue(str.contains("250,50") || str.contains("250.50"));
        assertTrue(str.contains("pedidosAtivos=3"));
    }
}
