package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class ApelidoInvalidoException extends NegocioException {

    public ApelidoInvalidoException() {
        super("O apelido escolhido contem termos nao permitidos.", HttpStatus.BAD_REQUEST);
    }
}
