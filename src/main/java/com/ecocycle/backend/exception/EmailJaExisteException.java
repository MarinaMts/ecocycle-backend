package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class EmailJaExisteException extends NegocioException {

    public EmailJaExisteException(String email) {
        super("Ja existe uma conta cadastrada com o e-mail informado.", HttpStatus.CONFLICT);
    }
}
