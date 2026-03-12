package com.organiza.application.usecase;

import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.domain.repository.PedidoRepositoryPort;

import java.time.Duration;
import java.time.LocalDateTime;
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
        return todosPedidos.stream()
                .flatMap(p -> p.getItens().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduto(),
                        Collectors.summingInt(item -> item.getQuantidade())
                ));
    }

    /**
     * Retorna estatísticas de receita (min, max, avg, count).
     */
    public DoubleSummaryStatistics estatisticasReceita() {
        return pedidoRepository.findByStatus(StatusPedido.PAGO).stream()
                .mapToDouble(Pedido::getTotal)
                .summaryStatistics();
    }

    /**
     * Conta pedidos ativos (não pagos).
     */
    public int contarPedidosAtivos() {
        return pedidoRepository.findAllActive().size();
    }
}
