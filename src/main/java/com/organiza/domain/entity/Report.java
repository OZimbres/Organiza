package com.organiza.domain.entity;

import com.organiza.domain.enums.StatusPedido;

import java.util.Map;
import java.util.Objects;

/**
 * Representa os dados agregados de um relatório de operações.
 * Valor imutável que encapsula métricas de receita, contagens de pedidos,
 * itens mais vendidos e estatísticas gerais.
 */
public class Report {

    private final double receitaTotal;
    private final Map<StatusPedido, Long> pedidosPorStatus;
    private final Map<String, Integer> itensMaisVendidos;
    private final RevenueStatistics estatisticas;
    private final int pedidosAtivos;

    public Report(double receitaTotal,
                  Map<StatusPedido, Long> pedidosPorStatus,
                  Map<String, Integer> itensMaisVendidos,
                  RevenueStatistics estatisticas,
                  int pedidosAtivos) {
        this.receitaTotal = receitaTotal;
        this.pedidosPorStatus = pedidosPorStatus != null ? Map.copyOf(pedidosPorStatus) : Map.of();
        this.itensMaisVendidos = itensMaisVendidos != null ? Map.copyOf(itensMaisVendidos) : Map.of();
        this.estatisticas = estatisticas;
        this.pedidosAtivos = pedidosAtivos;
    }

    public double getReceitaTotal() {
        return receitaTotal;
    }

    public Map<StatusPedido, Long> getPedidosPorStatus() {
        return pedidosPorStatus;
    }

    public Map<String, Integer> getItensMaisVendidos() {
        return itensMaisVendidos;
    }

    public RevenueStatistics getEstatisticas() {
        return estatisticas;
    }

    public int getPedidosAtivos() {
        return pedidosAtivos;
    }

    /**
     * Retorna o total geral de pedidos (soma de todas as contagens por status).
     */
    public long getTotalPedidos() {
        return pedidosPorStatus.values().stream().mapToLong(Long::longValue).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Double.compare(report.receitaTotal, receitaTotal) == 0
                && pedidosAtivos == report.pedidosAtivos
                && Objects.equals(pedidosPorStatus, report.pedidosPorStatus)
                && Objects.equals(itensMaisVendidos, report.itensMaisVendidos)
                && Objects.equals(estatisticas, report.estatisticas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receitaTotal, pedidosPorStatus, itensMaisVendidos, estatisticas, pedidosAtivos);
    }

    @Override
    public String toString() {
        return "Report{receita=" + String.format("%.2f", receitaTotal)
                + ", pedidosAtivos=" + pedidosAtivos
                + ", totalPedidos=" + getTotalPedidos()
                + "}";
    }
}
