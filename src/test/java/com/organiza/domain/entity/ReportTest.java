package com.organiza.domain.entity;

import com.organiza.domain.enums.StatusPedido;

import org.junit.jupiter.api.Test;

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
        RevenueStatistics stats = new RevenueStatistics(2, 25.0, 30.0, 55.0);

        Report report = new Report(150.0, porStatus, topItens, stats, 3);

        assertEquals(150.0, report.getReceitaTotal(), 0.001);
        assertEquals(3, report.getPedidosAtivos());
        assertEquals(8, report.getTotalPedidos());
        assertEquals(2, report.getItensMaisVendidos().size());
        assertEquals(10, report.getItensMaisVendidos().get("Café"));
        assertNotNull(report.getEstatisticas());
        assertEquals(2, report.getEstatisticas().count());
        assertEquals(25.0, report.getEstatisticas().min(), 0.001);
        assertEquals(30.0, report.getEstatisticas().max(), 0.001);
        assertEquals(55.0, report.getEstatisticas().sum(), 0.001);
        assertEquals(27.5, report.getEstatisticas().average(), 0.001);
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
        RevenueStatistics stats = new RevenueStatistics(5, 10.0, 50.0, 200.0);

        Report r1 = new Report(100.0, porStatus, topItens, stats, 2);
        Report r2 = new Report(100.0, porStatus, topItens, stats, 2);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveSerDiferenteComEstatisticasDiferentes() {
        Map<StatusPedido, Long> porStatus = Map.of(StatusPedido.PAGO, 5L);
        Map<String, Integer> topItens = Map.of("Café", 10);
        RevenueStatistics stats1 = new RevenueStatistics(5, 10.0, 50.0, 200.0);
        RevenueStatistics stats2 = new RevenueStatistics(3, 5.0, 100.0, 300.0);

        Report r1 = new Report(100.0, porStatus, topItens, stats1, 2);
        Report r2 = new Report(100.0, porStatus, topItens, stats2, 2);

        assertNotEquals(r1, r2);
    }

    @Test
    void deveRetornarToStringComResumo() {
        Report report = new Report(250.50, Map.of(StatusPedido.PAGO, 5L), Map.of(), null, 3);

        String str = report.toString();
        assertTrue(str.contains("250,50") || str.contains("250.50"));
        assertTrue(str.contains("pedidosAtivos=3"));
    }
}
