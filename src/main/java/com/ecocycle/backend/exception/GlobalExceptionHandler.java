package com.ecocycle.backend.exception;

import com.ecocycle.backend.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centraliza a conversao de excecoes em respostas HTTP padronizadas
 * (ver documento de decisoes tecnicas, secao 7.2).
 * Erros 500 nunca expoem stack trace ao cliente (secao 7.5).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Excecoes de negocio customizadas (400/401/403/404/409 conforme o caso)
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponse> handleNegocioException(NegocioException ex) {
        log.warn("Erro de negocio: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // Erros de validacao dos DTOs (@NotBlank, @Email, @Size, etc.) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroDeCampo)
                .toList();
        log.warn("Erro de validacao: {}", erros);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Dados invalidos.",
                erros
        );
        return ResponseEntity.badRequest().body(body);
    }

    private String formatarErroDeCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }

    // Argumentos invalidos (ex.: valor de enum inexistente) -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // Credenciais invalidas vindas do Spring Security -> 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentativa de autenticacao invalida.");
        ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "E-mail ou senha invalidos.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // Qualquer outra excecao nao mapeada -> 500 generico, sem vazar stack trace
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro interno nao tratado", ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno. Tente novamente mais tarde."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
