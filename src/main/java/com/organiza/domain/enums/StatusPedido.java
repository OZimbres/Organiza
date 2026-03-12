package com.organiza.domain.enums;

import java.util.Set;

/**
 * Status possíveis de um pedido com validação de transições.
 * Implementa o State Pattern para garantir que apenas transições válidas sejam permitidas.
 */
public enum StatusPedido {
    PENDENTE,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    PAGO;

    /**
     * Retorna o próximo status válido na sequência padrão.
     *
     * @return o próximo StatusPedido
     * @throws IllegalStateException se o pedido já estiver no estado final (PAGO)
     */
    public StatusPedido next() {
        return switch (this) {
            case PENDENTE -> EM_PREPARO;
            case EM_PREPARO -> PRONTO;
            case PRONTO -> ENTREGUE;
            case ENTREGUE -> PAGO;
            case PAGO -> throw new IllegalStateException("Pedido já está pago — não há próximo status");
        };
    }

    /**
     * Verifica se a transição para o status destino é permitida.
     */
    public boolean canTransitionTo(StatusPedido target) {
        return getAllowedTransitions().contains(target);
    }

    /**
     * Retorna o conjunto de transições permitidas a partir deste status.
     */
    public Set<StatusPedido> getAllowedTransitions() {
        return switch (this) {
            case PENDENTE -> Set.of(EM_PREPARO);
            case EM_PREPARO -> Set.of(PRONTO);
            case PRONTO -> Set.of(ENTREGUE);
            case ENTREGUE -> Set.of(PAGO);
            case PAGO -> Set.of();
        };
    }

    public String getLabel() {
        return switch (this) {
            case PENDENTE -> "Pendente";
            case EM_PREPARO -> "Em preparo";
            case PRONTO -> "Pronto";
            case ENTREGUE -> "Entregue";
            case PAGO -> "Pago";
        };
    }
}
