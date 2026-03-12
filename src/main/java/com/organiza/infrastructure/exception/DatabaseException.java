package com.organiza.infrastructure.exception;

/**
 * Exceção para erros de persistência e acesso a dados.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
