package com.ecocycle.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Classe base para os testes de integracao do EcoCycle Backend.
 *
 * Sobe o contexto Spring completo (incluindo o DataSeeder, que popula
 * trilhas/quizzes/figurinhas/pontos de coleta) e usa MockMvc para simular
 * requisicoes HTTP reais, passando pela cadeia de filtros de seguranca (JWT).
 *
 * As propriedades de src/test/resources/application.properties tem precedencia
 * automatica sobre src/main/resources durante os testes (sem necessidade de
 * @ActiveProfiles), o que ja desabilita o rate limiting do login e isola o
 * banco H2 de testes do banco usado em desenvolvimento normal.
 *
 * @Transactional aqui garante que cada metodo de teste roda dentro de uma
 * transacao que e desfeita (rollback) ao final - ou seja, um teste nunca
 * "suja" o banco para o proximo, mesmo reaproveitando o mesmo ApplicationContext
 * (e portanto o mesmo banco H2 em memoria) entre todas as classes de teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Registra um novo usuario com email aleatorio (evita colisao entre testes)
     * e retorna o token JWT ja pronto para uso no header Authorization.
     */
    protected String registrarERetornarToken() throws Exception {
        return registrarERetornarToken(emailAleatorio(), apelidoAleatorio(), "senha123");
    }

    protected String registrarERetornarToken(String email, String apelido, String senha) throws Exception {
        String payload = objectMapper.writeValueAsString(new RegisterPayload(email, apelido, senha));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("token").asText();
    }

    protected String emailAleatorio() {
        return "user" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }

    protected String apelidoAleatorio() {
        // 3-20 chars, apenas letras/numeros/underscore
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    // Classe auxiliar apenas para serializar o corpo do register nos testes
    private record RegisterPayload(String email, String apelido, String senha) {
    }

    /**
     * Sanity check: garante que o contexto Spring sobe corretamente
     * (inclusive o DataSeeder) antes de qualquer outro teste da suite.
     */
    @Test
    void contextoCarregaComSucesso() {
        // Se o @SpringBootTest nao subir, este teste (e todos os outros) falha no setup.
    }
}
