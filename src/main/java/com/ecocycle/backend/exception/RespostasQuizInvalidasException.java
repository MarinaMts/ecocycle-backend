package com.ecocycle.backend.exception;

import org.springframework.http.HttpStatus;

public class RespostasQuizInvalidasException extends NegocioException {

    public RespostasQuizInvalidasException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
