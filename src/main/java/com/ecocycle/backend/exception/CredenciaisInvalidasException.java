package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException extends NegocioException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha invalidos.", HttpStatus.UNAUTHORIZED);
    }
}
