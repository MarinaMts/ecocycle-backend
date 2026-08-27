package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void deveRetornarDadosDoUsuarioAutenticado() throws Exception {
        String email = emailAleatorio();
        String apelido = apelidoAleatorio();
        String token = registrarERetornarToken(email, apelido, "senha123");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.apelido").value(apelido));
    }

    @Test
    void deveRejeitarAcessoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveRejeitarAcessoComTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-invalido-qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAtualizarAvatarComSucesso() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(patch("/api/v1/users/me/avatar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\": \"AVATAR_07\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatar").value("AVATAR_07"));

        // Confirma que persistiu
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatar").value("AVATAR_07"));
    }

    @Test
    void deveRejeitarAvatarInvalido() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(patch("/api/v1/users/me/avatar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\": \"AVATAR_99\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveDesativarContaComSucesso() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conta desativada com sucesso."));
    }
}
