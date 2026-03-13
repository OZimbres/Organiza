package com.organiza.application.usecase;

import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.entity.Report;
import com.organiza.domain.entity.RevenueStatistics;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.domain.repository.PedidoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateReportsUseCaseTest {

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    private GenerateReportsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateReportsUseCase(pedidoRepository);
    }

    @Test
    void deveCalcularReceitaTotalComPedidosPagos() {
        Pedido p1 = criarPedido(1, StatusPedido.PAGO, "Café", 2, 5.00);
        Pedido p2 = criarPedido(2, StatusPedido.PAGO, "Pão", 1, 3.50);
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of(p1, p2));

        double receita = useCase.calcularReceitaTotal();

        assertEquals(13.50, receita, 0.001);
    }

    @Test
    void deveRetornarReceitaZeroSemPedidosPagos() {
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of());

        double receita = useCase.calcularReceitaTotal();

        assertEquals(0.0, receita, 0.001);
    }

    @Test
    void deveContarPedidosPorStatus() {
        when(pedidoRepository.findByStatus(StatusPedido.PENDENTE)).thenReturn(List.of(
                criarPedido(1, StatusPedido.PENDENTE, "Café", 1, 3.0),
                criarPedido(2, StatusPedido.PENDENTE, "Pão", 1, 2.0)
        ));
        when(pedidoRepository.findByStatus(StatusPedido.EM_PREPARO)).thenReturn(List.of(
                criarPedido(3, StatusPedido.EM_PREPARO, "Suco", 1, 4.0)
        ));
        when(pedidoRepository.findByStatus(StatusPedido.PRONTO)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.ENTREGUE)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of());

        Map<StatusPedido, Long> result = useCase.contarPedidosPorStatus();

        assertEquals(2L, result.get(StatusPedido.PENDENTE));
        assertEquals(1L, result.get(StatusPedido.EM_PREPARO));
        assertEquals(0L, result.get(StatusPedido.PRONTO));
        assertEquals(0L, result.get(StatusPedido.ENTREGUE));
        assertEquals(0L, result.get(StatusPedido.PAGO));
    }

    @Test
    void deveListarItensMaisVendidos() {
        Pedido p1 = criarPedido(1, StatusPedido.PAGO, "Café", 3, 5.00);
        Pedido p2 = criarPedido(2, StatusPedido.PAGO, "Café", 2, 5.00);
        p2.addItem(new ItemPedido("Pão", 1, 3.50));
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of(p1, p2));

        Map<String, Integer> topItens = useCase.itensMaisVendidos();

        assertEquals(5, topItens.get("Café"));
        assertEquals(1, topItens.get("Pão"));
    }

    @Test
    void deveRetornarMapaVazioSemItensPagos() {
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of());

        Map<String, Integer> topItens = useCase.itensMaisVendidos();

        assertTrue(topItens.isEmpty());
    }

    @Test
    void deveCalcularEstatisticasReceita() {
        Pedido p1 = criarPedido(1, StatusPedido.PAGO, "Café", 2, 5.00);
        Pedido p2 = criarPedido(2, StatusPedido.PAGO, "Pão", 1, 10.00);
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of(p1, p2));

        RevenueStatistics stats = useCase.estatisticasReceita();

        assertEquals(2, stats.count());
        assertEquals(10.0, stats.min(), 0.001);
        assertEquals(10.0, stats.max(), 0.001);
        assertEquals(10.0, stats.average(), 0.001);
    }

    @Test
    void deveRetornarEstatisticasVaziasSemPedidos() {
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of());

        RevenueStatistics stats = useCase.estatisticasReceita();

        assertEquals(0, stats.count());
        assertEquals(0.0, stats.average(), 0.001);
    }

    @Test
    void deveContarPedidosAtivos() {
        when(pedidoRepository.findAllActive()).thenReturn(List.of(
                criarPedido(1, StatusPedido.PENDENTE, "Café", 1, 3.0),
                criarPedido(2, StatusPedido.EM_PREPARO, "Pão", 1, 2.0),
                criarPedido(3, StatusPedido.PRONTO, "Suco", 1, 4.0)
        ));

        int count = useCase.contarPedidosAtivos();

        assertEquals(3, count);
    }

    @Test
    void deveRetornarZeroPedidosAtivos() {
        when(pedidoRepository.findAllActive()).thenReturn(List.of());

        int count = useCase.contarPedidosAtivos();

        assertEquals(0, count);
    }

    @Test
    void deveGerarRelatorioCompleto() {
        Pedido pago = criarPedido(1, StatusPedido.PAGO, "Café", 2, 5.00);
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of(pago));
        when(pedidoRepository.findByStatus(StatusPedido.PENDENTE)).thenReturn(List.of(
                criarPedido(2, StatusPedido.PENDENTE, "Pão", 1, 3.00)
        ));
        when(pedidoRepository.findByStatus(StatusPedido.EM_PREPARO)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.PRONTO)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.ENTREGUE)).thenReturn(List.of());
        when(pedidoRepository.findAllActive()).thenReturn(List.of(
                criarPedido(2, StatusPedido.PENDENTE, "Pão", 1, 3.00)
        ));

        Report report = useCase.gerarRelatorio();

        assertNotNull(report);
        assertEquals(10.0, report.getReceitaTotal(), 0.001);
        assertEquals(1, report.getPedidosAtivos());
        assertEquals(2, report.getTotalPedidos());
        assertEquals(1, report.getPedidosPorStatus().get(StatusPedido.PAGO));
        assertEquals(1, report.getPedidosPorStatus().get(StatusPedido.PENDENTE));
        assertEquals(2, report.getItensMaisVendidos().get("Café"));
        assertNotNull(report.getEstatisticas());
        assertEquals(1, report.getEstatisticas().count());
        assertEquals(10.0, report.getEstatisticas().sum(), 0.001);
    }

    @Test
    void gerarRelatorioDeveBuscarPedidosPagosUmaUnicaVez() {
        when(pedidoRepository.findByStatus(StatusPedido.PAGO)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.PENDENTE)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.EM_PREPARO)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.PRONTO)).thenReturn(List.of());
        when(pedidoRepository.findByStatus(StatusPedido.ENTREGUE)).thenReturn(List.of());
        when(pedidoRepository.findAllActive()).thenReturn(List.of());

        useCase.gerarRelatorio();

        // Paid orders should be fetched only once (optimized), others once each
        verify(pedidoRepository, times(1)).findByStatus(StatusPedido.PAGO);
    }

    private Pedido criarPedido(int id, StatusPedido status, String produto, int qtd, double preco) {
        Pedido pedido = new Pedido(id, 1, "Cliente", status, LocalDateTime.now());
        pedido.addItem(new ItemPedido(produto, qtd, preco));
        return pedido;
    }
}
