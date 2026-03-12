package com.organiza.application.dto;

/**
 * DTO imutável para transferência de dados de item de pedido.
 */
public record OrderItemDTO(int id, String produto, int quantidade, double preco, double subtotal) {
}
