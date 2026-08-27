package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao base para erros de regra de negocio.
 * Toda excecao customizada da aplicacao deve estender esta classe,
 * para que o GlobalExceptionHandler saiba qual status HTTP retornar.
 */
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
