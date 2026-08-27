package com.ecocycle.backend.security;

import com.ecocycle.backend.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Retorna 401 no formato de erro padronizado quando uma rota protegida
 * e acessada sem autenticacao valida (em vez do HTML padrao do Spring).
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // findAndRegisterModules() registra o JavaTimeModule, mas por padrao o Jackson
    // ainda serializa LocalDateTime como array de numeros - desabilitamos isso para
    // manter o mesmo formato ISO-8601 usado pelo restante da API (GlobalExceptionHandler).
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Autenticacao necessaria ou token invalido/expirado."
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
