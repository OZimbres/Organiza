package com.organiza.model;

/**
 * Status possíveis de um pedido.
 */
public enum StatusPedido {
    PENDENTE,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    PAGO;

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
