package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class ConteudoNaoLidoException extends NegocioException {

    public ConteudoNaoLidoException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
