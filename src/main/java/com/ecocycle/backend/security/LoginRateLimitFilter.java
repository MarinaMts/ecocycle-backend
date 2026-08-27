package com.ecocycle.backend.security;

import com.ecocycle.backend.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting BASICO (em memoria) no endpoint de login, para mitigar forca bruta
 * (ver documento de decisoes tecnicas, secao 7.5).
 * Limitacao conhecida: reseta ao reiniciar a aplicacao e nao e distribuido entre
 * multiplas instancias - suficiente para o escopo de TCC, nao para producao em escala.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final int MAX_TENTATIVAS = 10;
    private static final long JANELA_MS = 60_000; // 1 minuto

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final ConcurrentHashMap<String, Janela> tentativasPorIp = new ConcurrentHashMap<>();

    // Permite desabilitar o rate limiting nos testes de integracao
    // (src/test/resources/application.properties define
    // app.rate-limit.login.enabled=false), ja que o filtro e um singleton cuja
    // contagem persiste durante toda a suite - sem isso, muitos testes de /login em
    // sequencia acabariam esbarrando no limite artificialmente.
    @Value("${app.rate-limit.login.enabled:true}")
    private boolean rateLimitHabilitado;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!rateLimitHabilitado || !LOGIN_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = obterIpCliente(request);
        long agora = Instant.now().toEpochMilli();

        Janela janela = tentativasPorIp.computeIfAbsent(ip, k -> new Janela(agora));

        synchronized (janela) {
            if (agora - janela.inicioMs > JANELA_MS) {
                janela.inicioMs = agora;
                janela.contador = 0;
            }
            janela.contador++;

            if (janela.contador > MAX_TENTATIVAS) {
                log.warn("Rate limit excedido para tentativas de login. IP: {}", ip);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ErrorResponse body = new ErrorResponse(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Muitas tentativas de login. Tente novamente em instantes."
                );
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String obterIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Janela {
        long inicioMs;
        int contador;

        Janela(long inicioMs) {
            this.inicioMs = inicioMs;
            this.contador = 0;
        }
    }
}
