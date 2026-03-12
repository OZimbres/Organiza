package com.organiza.domain.repository;

import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusPedido;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) para operações de persistência de pedidos.
 * Segue o princípio da Arquitetura Hexagonal — o domínio define o contrato,
 * e a infraestrutura fornece a implementação.
 */
public interface PedidoRepositoryPort {

    Pedido save(Pedido pedido);

    Optional<Pedido> findById(int id);

    List<Pedido> findByMesaId(int mesaId);

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findAllActive();

    void updateStatus(int id, StatusPedido status);

    void deleteById(int id);

    List<ItemPedido> findItensByPedidoId(int pedidoId);
}
