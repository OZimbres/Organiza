package com.organiza.application.usecase;

import com.organiza.application.dto.OrderDTO;
import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusMesa;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.domain.repository.MesaRepositoryPort;
import com.organiza.domain.repository.PedidoRepositoryPort;
import com.organiza.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusUseCaseTest {

    @Mock
    private MesaRepositoryPort mesaRepository;

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    private UpdateOrderStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateOrderStatusUseCase(mesaRepository, pedidoRepository);
    }

    @Test
    void deveAvancarDePendenteParaEmPreparo() {
        Pedido pedido = new Pedido(1, 1, "Ana", StatusPedido.PENDENTE, LocalDateTime.now());
        pedido.addItem(new ItemPedido("Café", 1, 3.00));
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        OrderDTO result = useCase.execute(1);

        assertEquals("EM_PREPARO", result.status());
        verify(pedidoRepository).updateStatus(1, StatusPedido.EM_PREPARO);
    }

    @Test
    void deveAvancarDeEmPreparoParaPronto() {
        Pedido pedido = new Pedido(1, 1, "Ana", StatusPedido.EM_PREPARO, LocalDateTime.now());
        pedido.addItem(new ItemPedido("Café", 1, 3.00));
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        OrderDTO result = useCase.execute(1);

        assertEquals("PRONTO", result.status());
        verify(pedidoRepository).updateStatus(1, StatusPedido.PRONTO);
    }

    @Test
    void deveLiberarMesaQuandoTodosPedidosPagos() {
        Pedido pedido = new Pedido(1, 5, "Carlos", StatusPedido.ENTREGUE, LocalDateTime.now());
        pedido.addItem(new ItemPedido("Café", 1, 3.00));
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        Pedido pedidoPago = new Pedido(1, 5, "Carlos", StatusPedido.PAGO, LocalDateTime.now());
        when(pedidoRepository.findByMesaId(5)).thenReturn(List.of(pedidoPago));

        useCase.execute(1);

        verify(pedidoRepository).updateStatus(1, StatusPedido.PAGO);
        verify(mesaRepository).updateStatus(5, StatusMesa.LIVRE);
    }

    @Test
    void naoDeveLiberarMesaComPedidosPendentes() {
        Pedido pedido = new Pedido(1, 5, "Carlos", StatusPedido.ENTREGUE, LocalDateTime.now());
        pedido.addItem(new ItemPedido("Café", 1, 3.00));
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        Pedido pedidoPago = new Pedido(1, 5, "Carlos", StatusPedido.PAGO, LocalDateTime.now());
        Pedido outroPendente = new Pedido(2, 5, "Maria", StatusPedido.PENDENTE, LocalDateTime.now());
        when(pedidoRepository.findByMesaId(5)).thenReturn(List.of(pedidoPago, outroPendente));

        useCase.execute(1);

        verify(mesaRepository, never()).updateStatus(eq(5), eq(StatusMesa.LIVRE));
    }

    @Test
    void deveLancarExcecaoParaPedidoInexistente() {
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> useCase.execute(999));
    }

    @Test
    void deveLancarExcecaoAoAvancarPedidoPago() {
        Pedido pedido = new Pedido(1, 1, "Ana", StatusPedido.PAGO, LocalDateTime.now());
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> useCase.execute(1));
    }
}
