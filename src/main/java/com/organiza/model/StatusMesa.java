package com.organiza.model;

/**
 * Status possíveis de uma mesa.
 */
public enum StatusMesa {
    LIVRE,
    OCUPADA;

    public String getLabel() {
        return switch (this) {
            case LIVRE -> "Livre";
            case OCUPADA -> "Ocupada";
        };
    }
}
