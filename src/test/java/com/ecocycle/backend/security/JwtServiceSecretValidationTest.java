package com.ecocycle.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fail fast do segredo JWT: sem segredo valido o JwtService nao e criado e,
 * portanto, a aplicacao nao sobe.
 */
class JwtServiceSecretValidationTest {

    private static final long EXPIRACAO = 604800000L;

    @Test
    void deveFalharQuandoSecretAusente() {
        assertThatThrownBy(() -> new JwtService(null, EXPIRACAO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void deveFalharQuandoSecretEmBranco() {
        assertThatThrownBy(() -> new JwtService("   ", EXPIRACAO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nao configurado");
    }

    @Test
    void deveFalharQuandoSecretMenorQue32Bytes() {
        String curto = "a".repeat(31);

        assertThatThrownBy(() -> new JwtService(curto, EXPIRACAO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("31 bytes")
                .hasMessageContaining("minimo 32");
    }

    @Test
    void deveAceitarSecretCom32BytesOuMais() {
        assertThatCode(() -> new JwtService("a".repeat(32), EXPIRACAO)).doesNotThrowAnyException();
    }

    @Test
    void perfilProdNaoDeveTerFallbackParaOSecret() throws Exception {
        Properties prod = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-prod.properties"));

        assertThat(prod.getProperty("jwt.secret")).isEqualTo("${JWT_SECRET}");
    }
}
