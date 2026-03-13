package com.organiza.domain.entity;

import java.util.Objects;

/**
 * Snapshot imutável de estatísticas de receita.
 * Substitui DoubleSummaryStatistics para garantir imutabilidade no Report.
 */
public record RevenueStatistics(long count, double min, double max, double sum) {

    /**
     * Retorna a média (sum / count), ou 0.0 se não houver dados.
     */
    public double average() {
        return count > 0 ? sum / count : 0.0;
    }

    /**
     * Cria um RevenueStatistics vazio (sem dados).
     */
    public static RevenueStatistics empty() {
        return new RevenueStatistics(0, 0.0, 0.0, 0.0);
    }

    @Override
    public String toString() {
        return "RevenueStatistics{count=" + count
                + ", min=" + String.format("%.2f", min)
                + ", max=" + String.format("%.2f", max)
                + ", sum=" + String.format("%.2f", sum)
                + ", avg=" + String.format("%.2f", average())
                + "}";
    }
}
