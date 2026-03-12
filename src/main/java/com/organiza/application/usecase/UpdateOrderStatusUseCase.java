package com.organiza.application.usecase;

import com.organiza.application.dto.OrderDTO;
import com.organiza.application.dto.OrderItemDTO;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusMesa;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.domain.repository.MesaRepositoryPort;
import com.organiza.domain.repository.PedidoRepositoryPort;
import com.organiza.infrastructure.exception.BusinessException;

import java.util.List;

/**
 * Caso de uso para atualização do status de um pedido.
 * Implementa o State Pattern para validar transições de status e liberar mesas quando completo.
 */
public class UpdateOrderStatusUseCase {

    private final MesaRepositoryPort mesaRepository;
    private final PedidoRepositoryPort pedidoRepository;

    public UpdateOrderStatusUseCase(MesaRepositoryPort mesaRepository, PedidoRepositoryPort pedidoRepository) {
        this.mesaRepository = mesaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Avança o status do pedido para o próximo estágio.
     *
     * @param pedidoId ID do pedido
     * @return DTO do pedido atualizado
     */
    public OrderDTO execute(int pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new BusinessException("Pedido não encontrado: " + pedidoId));

        StatusPedido novoStatus = pedido.getStatus().next();
        pedidoRepository.updateStatus(pedidoId, novoStatus);
        pedido.setStatus(novoStatus);

        if (novoStatus == StatusPedido.PAGO) {
            liberarMesaSeCompleta(pedido.getMesaId());
        }

        return toDTO(pedido);
    }

    private void liberarMesaSeCompleta(int mesaId) {
        List<Pedido> pedidosMesa = pedidoRepository.findByMesaId(mesaId);
        boolean todosPagos = pedidosMesa.stream()
                .allMatch(p -> p.getStatus() == StatusPedido.PAGO);
        if (todosPagos) {
            mesaRepository.updateStatus(mesaId, StatusMesa.LIVRE);
        }
    }

    private OrderDTO toDTO(Pedido pedido) {
        List<OrderItemDTO> itemDTOs = pedido.getItens().stream()
                .map(i -> new OrderItemDTO(i.getId(), i.getProduto(), i.getQuantidade(), i.getPreco(), i.getSubtotal()))
                .toList();

        return new OrderDTO(
                pedido.getId(),
                pedido.getMesaId(),
                pedido.getNomeCliente(),
                pedido.getStatus().name(),
                pedido.getStatus().getLabel(),
                pedido.getDataHora(),
                itemDTOs,
                pedido.getTotal()
        );
    }
}
