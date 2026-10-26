package com.ecocycle.backend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Deve ser o filtro mais externo da cadeia de seguranca (ver addFilterBefore
 * em SecurityConfig). Garante um requestId para toda requisicao (gerado ou
 * propagado via header X-Request-Id), loga uma linha de access log ao final
 * e limpa o MDC inteiro no finally - ponto unico de limpeza para toda a
 * requisicao (cobre tambem o que o JwtAuthenticationFilter adicionar ao MDC),
 * evitando leak de contexto entre requisicoes em threads reaproveitadas pelo
 * servidor.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("http.access");
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";
    // O X-Request-Id vem do cliente: so e aceito se for curto e com caracteres
    // seguros (evita log injection e headers gigantes); senao, gera um novo.
    private static final Pattern REQUEST_ID_VALIDO = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || !REQUEST_ID_VALIDO.matcher(requestId).matches()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);

        long inicioNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = (System.nanoTime() - inicioNanos) / 1_000_000;
            ACCESS_LOG.info(
                    "{} {} status={} duracaoMs={} ip={} userAgent={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duracaoMs,
                    HttpRequestUtils.obterIpCliente(request),
                    request.getHeader("User-Agent")
            );
            MDC.clear();
        }
    }
}
