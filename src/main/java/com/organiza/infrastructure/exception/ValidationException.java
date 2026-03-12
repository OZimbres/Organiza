package com.organiza.infrastructure.exception;

/**
 * Exceção para erros de validação de dados de entrada.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
