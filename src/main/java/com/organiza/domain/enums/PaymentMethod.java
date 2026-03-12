package com.organiza.domain.enums;

/**
 * Métodos de pagamento suportados pelo sistema.
 */
public enum PaymentMethod {
    DINHEIRO,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    PIX;

    public String getLabel() {
        return switch (this) {
            case DINHEIRO -> "Dinheiro";
            case CARTAO_CREDITO -> "Cartão de Crédito";
            case CARTAO_DEBITO -> "Cartão de Débito";
            case PIX -> "Pix";
        };
    }
}
