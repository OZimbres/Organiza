package com.organiza.application.usecase;

import com.organiza.application.dto.OrderDTO;
import com.organiza.application.dto.OrderItemDTO;
import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Mesa;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusMesa;
import com.organiza.domain.repository.MesaRepositoryPort;
import com.organiza.domain.repository.PedidoRepositoryPort;
import com.organiza.infrastructure.exception.BusinessException;
import com.organiza.infrastructure.exception.ValidationException;

import java.util.List;

/**
 * Caso de uso para criação de pedidos.
 * Valida dados de entrada, persiste o pedido e marca a mesa como ocupada.
 */
public class CreateOrderUseCase {

    private final MesaRepositoryPort mesaRepository;
    private final PedidoRepositoryPort pedidoRepository;

    public CreateOrderUseCase(MesaRepositoryPort mesaRepository, PedidoRepositoryPort pedidoRepository) {
        this.mesaRepository = mesaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Executa a criação de um novo pedido.
     *
     * @param mesaId      ID da mesa
     * @param nomeCliente nome do cliente
     * @param itens       lista de itens do pedido
     * @return DTO do pedido criado
     */
    public OrderDTO execute(int mesaId, String nomeCliente, List<ItemPedido> itens) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new BusinessException("Mesa não encontrada: " + mesaId));

        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new ValidationException("Nome do cliente é obrigatório");
        }

        if (itens == null || itens.isEmpty()) {
            throw new ValidationException("Pedido deve conter ao menos um item");
        }

        Pedido pedido = new Pedido(mesa.getId(), nomeCliente);
        itens.forEach(pedido::addItem);

        Pedido saved = pedidoRepository.save(pedido);
        mesaRepository.updateStatus(mesa.getId(), StatusMesa.OCUPADA);

        return toDTO(saved);
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
