package com.ecocycle.backend.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ecocycle.backend.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida o CorrelationIdFilter: requestId presente em todo log de uma
 * requisicao, diferente entre requisicoes distintas, propagado no header de
 * resposta, e sem leak de MDC entre chamadas (MockMvc executa na mesma thread
 * do teste, por isso MDC.getCopyOfContextMap() apos a chamada e um proxy
 * razoavel para "a thread do pool nao herda contexto da requisicao anterior").
 */
class CorrelationIdFilterIntegrationTest extends BaseIntegrationTest {

    private Logger accessLogger;
    private ListAppender<ILoggingEvent> appender;
    private Level nivelOriginal;

    @BeforeEach
    void anexarAppender() {
        accessLogger = (Logger) LoggerFactory.getLogger("http.access");
        nivelOriginal = accessLogger.getLevel();
        // src/test/resources/application.properties define esse logger como WARN
        // para nao poluir a suite; aqui elevamos para capturar o INFO do access log.
        accessLogger.setLevel(Level.ALL);
        appender = new ListAppender<>();
        appender.start();
        accessLogger.addAppender(appender);
    }

    @AfterEach
    void desanexarAppender() {
        accessLogger.detachAppender(appender);
        accessLogger.setLevel(nivelOriginal);
    }

    @Test
    void deveGerarRequestIdDiferenteEntreRequisicoesSemLeakDeMdc() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // O helper registrarERetornarToken() tambem gera uma linha de access log
        // (POST /auth/register, publica); as duas chamadas a /me sao as DUAS ULTIMAS.
        List<ILoggingEvent> eventos = appender.list;
        assertThat(eventos).hasSizeGreaterThanOrEqualTo(3);

        ILoggingEvent eventoMe1 = eventos.get(eventos.size() - 2);
        ILoggingEvent eventoMe2 = eventos.get(eventos.size() - 1);

        String requestId1 = eventoMe1.getMDCPropertyMap().get("requestId");
        String requestId2 = eventoMe2.getMDCPropertyMap().get("requestId");

        assertThat(requestId1).isNotBlank();
        assertThat(requestId2).isNotBlank();
        assertThat(requestId1).isNotEqualTo(requestId2);
        assertThat(eventoMe1.getMDCPropertyMap()).containsKey("userId");
        assertThat(eventoMe2.getMDCPropertyMap()).containsKey("userId");

        // Sem leak: a thread do teste nao deve carregar MDC apos a requisicao concluida.
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void naoDeveIncluirUsuarioNoMdcParaRotaPublica() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        ILoggingEvent evento = appender.list.get(appender.list.size() - 1);
        assertThat(evento.getMDCPropertyMap()).doesNotContainKey("userId");
    }

    @Test
    void deveEcoarRequestIdRecebidoNoHeaderDeResposta() throws Exception {
        String meuRequestId = "teste-request-id-123";

        mockMvc.perform(get("/actuator/health").header("X-Request-Id", meuRequestId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", meuRequestId));
    }

    @Test
    void deveGerarRequestIdQuandoClienteNaoEnvia() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void deveDescartarRequestIdComCaracteresInvalidos() throws Exception {
        // CR/LF ja sao barrados antes pelo StrictHttpFirewall (400); aqui o caso
        // sao caracteres que passam pelo firewall mas nao devem ir para o log.
        String malicioso = "abc INJETADO status=200 <script>";

        mockMvc.perform(get("/actuator/health").header("X-Request-Id", malicioso))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", not(malicioso)));

        String requestIdLogado = appender.list.get(appender.list.size() - 1).getMDCPropertyMap().get("requestId");
        assertThat(requestIdLogado).isNotEqualTo(malicioso).doesNotContain(" ");
    }

    @Test
    void deveDescartarRequestIdMuitoLongo() throws Exception {
        String longo = "a".repeat(65);

        mockMvc.perform(get("/actuator/health").header("X-Request-Id", longo))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", not(longo)));
    }

    @Test
    void deveExecutarFiltroUmaUnicaVezPorRequisicao() throws Exception {
        int antes = appender.list.size();

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        assertThat(appender.list.size() - antes).isEqualTo(1);
    }
}
