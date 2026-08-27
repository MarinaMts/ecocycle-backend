package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConteudoEducativoControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void deveListarAsDuasTrilhasComSeusConteudos() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/trilhas")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].trilha").value("OS_4RS"))
                .andExpect(jsonPath("$.data[0].conteudos.length()").value(4))
                .andExpect(jsonPath("$.data[1].trilha").value("LIXO_ELETRONICO"))
                .andExpect(jsonPath("$.data[1].conteudos.length()").value(3));
    }

    @Test
    void trilhasDevemComecarComTudoNaoLido() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/trilhas")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].conteudos[0].lido").value(false))
                .andExpect(jsonPath("$.data[0].conteudos[0].quizConcluido").value(false));
    }

    @Test
    void deveRetornarDetalheDoConteudoComTextoCompleto() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/conteudos/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codigo").value("EDU-01"))
                .andExpect(jsonPath("$.data.titulo").value("Reciclar"))
                .andExpect(jsonPath("$.data.corpo").isNotEmpty())
                .andExpect(jsonPath("$.data.lido").value(false));
    }

    @Test
    void deveRetornar404ParaConteudoInexistente() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/conteudos/9999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveMarcarConteudoComoLido() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(post("/api/v1/conteudos/1/marcar-lido")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lido").value(true));

        // Reflete na listagem de trilhas tambem
        mockMvc.perform(get("/api/v1/trilhas")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].conteudos[0].lido").value(true));
    }

    @Test
    void marcarComoLidoDuasVezesNaoDeveDuplicarNemFalhar() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(post("/api/v1/conteudos/1/marcar-lido")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // Marcar de novo deve ser idempotente - continua 200, continua lido=true
        mockMvc.perform(post("/api/v1/conteudos/1/marcar-lido")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lido").value(true));
    }
}
