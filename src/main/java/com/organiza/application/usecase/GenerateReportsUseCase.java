package com.organiza.application.usecase;

import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.entity.Report;
import com.organiza.domain.entity.RevenueStatistics;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.domain.repository.PedidoRepositoryPort;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caso de uso para geração de relatórios e analytics.
 * Fornece métricas sobre receita, pedidos e tempo de preparo.
 */
public class GenerateReportsUseCase {

    private final PedidoRepositoryPort pedidoRepository;

    public GenerateReportsUseCase(PedidoRepositoryPort pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Calcula a receita total de pedidos pagos.
     */
    public double calcularReceitaTotal() {
        return pedidoRepository.findByStatus(StatusPedido.PAGO).stream()
                .mapToDouble(Pedido::getTotal)
                .sum();
    }

    /**
     * Conta o total de pedidos por status.
     */
    public Map<StatusPedido, Long> contarPedidosPorStatus() {
        Map<StatusPedido, Long> result = new java.util.EnumMap<>(StatusPedido.class);
        for (StatusPedido status : StatusPedido.values()) {
            result.put(status, (long) pedidoRepository.findByStatus(status).size());
        }
        return result;
    }

    /**
     * Lista os itens mais vendidos com suas quantidades totais.
     */
    public Map<String, Integer> itensMaisVendidos() {
        List<Pedido> todosPedidos = pedidoRepository.findByStatus(StatusPedido.PAGO);
        return itensMaisVendidos(todosPedidos);
    }

    /**
     * Retorna estatísticas de receita (min, max, avg, count) como snapshot imutável.
     */
    public RevenueStatistics estatisticasReceita() {
        return estatisticasReceita(pedidoRepository.findByStatus(StatusPedido.PAGO));
    }

    /**
     * Conta pedidos ativos (não pagos).
     */
    public int contarPedidosAtivos() {
        return pedidoRepository.findAllActive().size();
    }

    /**
     * Gera um relatório completo com todas as métricas agregadas.
     * Busca pedidos pagos uma única vez e reutiliza para receita, itens e estatísticas,
     * evitando consultas redundantes ao repositório.
     */
    public Report gerarRelatorio() {
        List<Pedido> pedidosPagos = pedidoRepository.findByStatus(StatusPedido.PAGO);

        double receita = pedidosPagos.stream().mapToDouble(Pedido::getTotal).sum();

        Map<StatusPedido, Long> porStatus = new java.util.EnumMap<>(StatusPedido.class);
        porStatus.put(StatusPedido.PAGO, (long) pedidosPagos.size());
        for (StatusPedido status : StatusPedido.values()) {
            if (status != StatusPedido.PAGO) {
                porStatus.put(status, (long) pedidoRepository.findByStatus(status).size());
            }
        }

        Map<String, Integer> topItens = itensMaisVendidos(pedidosPagos);
        RevenueStatistics stats = estatisticasReceita(pedidosPagos);
        int ativos = pedidoRepository.findAllActive().size();

        return new Report(receita, porStatus, topItens, stats, ativos);
    }

    private Map<String, Integer> itensMaisVendidos(List<Pedido> pedidosPagos) {
        return pedidosPagos.stream()
                .flatMap(p -> p.getItens().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduto(),
                        Collectors.summingInt(ItemPedido::getQuantidade)
                ));
    }

    private RevenueStatistics estatisticasReceita(List<Pedido> pedidosPagos) {
        DoubleSummaryStatistics dss = pedidosPagos.stream()
                .mapToDouble(Pedido::getTotal)
                .summaryStatistics();
        if (dss.getCount() == 0) {
            return RevenueStatistics.empty();
        }
        return new RevenueStatistics(dss.getCount(), dss.getMin(), dss.getMax(), dss.getSum());
    }
}
