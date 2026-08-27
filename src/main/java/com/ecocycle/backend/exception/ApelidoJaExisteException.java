package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class ApelidoJaExisteException extends NegocioException {

    public ApelidoJaExisteException(String apelido) {
        super("O apelido '" + apelido + "' ja esta em uso.", HttpStatus.CONFLICT);
    }
}
