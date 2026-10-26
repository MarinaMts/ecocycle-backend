package com.ecocycle.backend.logging;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracao do IP real do cliente, compartilhada entre filtros que precisam
 * dessa informacao (rate limiting, access log).
 */
public final class HttpRequestUtils {

    private HttpRequestUtils() {
    }

    public static String obterIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
