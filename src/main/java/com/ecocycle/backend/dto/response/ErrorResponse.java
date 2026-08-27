package com.ecocycle.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato de erro padronizado (ver documento de decisoes tecnicas, secao 7.2):
 * { status, message, timestamp }
 * O campo "errors" e opcional, usado para detalhar erros de validacao (Bean Validation).
 */
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message, List<String> errors) {
        this(status, message);
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
