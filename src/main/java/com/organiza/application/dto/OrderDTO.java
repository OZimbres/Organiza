package com.organiza.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO imutável para transferência de dados de pedido.
 */
public record OrderDTO(
        int id,
        int mesaId,
        String nomeCliente,
        String status,
        String statusLabel,
        LocalDateTime dataHora,
        List<OrderItemDTO> itens,
        double total
) {
}
