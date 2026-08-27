package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void deveRegistrarUsuarioComSucesso() throws Exception {
        String email = emailAleatorio();
        String apelido = apelidoAleatorio();

        String payload = """
                {"email": "%s", "apelido": "%s", "senha": "senha123"}
                """.formatted(email, apelido);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.apelido").value(apelido))
                .andExpect(jsonPath("$.data.user.avatar").value("AVATAR_01"));
    }

    @Test
    void deveRejeitarRegistroComEmailDuplicado() throws Exception {
        String email = emailAleatorio();
        String payload = """
                {"email": "%s", "apelido": "%s", "senha": "senha123"}
                """.formatted(email, apelidoAleatorio());

        // primeiro registro - sucesso
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // segundo registro com o MESMO email, apelido diferente - deve falhar com 409
        String payloadDuplicado = """
                {"email": "%s", "apelido": "%s", "senha": "outrasenha"}
                """.formatted(email, apelidoAleatorio());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadDuplicado))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRejeitarRegistroComApelidoDuplicado() throws Exception {
        String apelido = apelidoAleatorio();
        String payload1 = """
                {"email": "%s", "apelido": "%s", "senha": "senha123"}
                """.formatted(emailAleatorio(), apelido);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload1))
                .andExpect(status().isCreated());

        String payload2 = """
                {"email": "%s", "apelido": "%s", "senha": "senha123"}
                """.formatted(emailAleatorio(), apelido);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarRegistroComSenhaCurta() throws Exception {
        String payload = """
                {"email": "%s", "apelido": "%s", "senha": "123"}
                """.formatted(emailAleatorio(), apelidoAleatorio());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarRegistroComEmailInvalido() throws Exception {
        String payload = """
                {"email": "nao-e-um-email", "apelido": "%s", "senha": "senha123"}
                """.formatted(apelidoAleatorio());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveLogarComSucessoAposRegistro() throws Exception {
        String email = emailAleatorio();
        String senha = "senha123";
        String payloadRegister = """
                {"email": "%s", "apelido": "%s", "senha": "%s"}
                """.formatted(email, apelidoAleatorio(), senha);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRegister))
                .andExpect(status().isCreated());

        String payloadLogin = """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("data").get("user").get("email").asText()).isEqualTo(email);
    }

    @Test
    void deveRejeitarLoginComSenhaErrada() throws Exception {
        String email = emailAleatorio();
        String payloadRegister = """
                {"email": "%s", "apelido": "%s", "senha": "senhaCorreta"}
                """.formatted(email, apelidoAleatorio());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRegister))
                .andExpect(status().isCreated());

        String payloadLogin = """
                {"email": "%s", "senha": "senhaErrada"}
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveRejeitarLoginComEmailInexistente() throws Exception {
        String payloadLogin = """
                {"email": "%s", "senha": "qualquersenha"}
                """.formatted(emailAleatorio());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadLogin))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarLoginAposDesativarConta() throws Exception {
        String email = emailAleatorio();
        String senha = "senha123";
        String payloadRegister = """
                {"email": "%s", "apelido": "%s", "senha": "%s"}
                """.formatted(email, apelidoAleatorio(), senha);

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRegister))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = json.get("data").get("token").asText();

        // Desativa a propria conta (soft delete)
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // Login subsequente deve falhar com 401 - mesma mensagem generica de
        // credenciais invalidas, sem revelar que a conta foi desativada
        // (ver ScannerService/AuthService - decisao de seguranca proposital)
        String payloadLogin = """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadLogin))
                .andExpect(status().isUnauthorized());
    }
}
