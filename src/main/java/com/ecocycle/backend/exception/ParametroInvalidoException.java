package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class ParametroInvalidoException extends NegocioException {

    public ParametroInvalidoException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
