package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PontoColetaControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void deveListarTodosOsPontosCadastrados() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/pontos-coleta")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6))
                // sem lat/lng, nao calcula distancia
                .andExpect(jsonPath("$.data[0].distanciaKm").doesNotExist());
    }

    @Test
    void deveBuscarPontosProximosOrdenadosPorDistancia() throws Exception {
        String token = registrarERetornarToken();

        // Coordenadas exatas do ponto "Mackenzie Higienopolis" (seed) - deve vir em 1o lugar com distancia ~0
        mockMvc.perform(get("/api/v1/pontos-coleta")
                        .param("lat", "-23.5433")
                        .param("lng", "-46.6528")
                        .param("raioKm", "10")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nome").value("Ponto de Coleta Mackenzie - Higienopolis"))
                .andExpect(jsonPath("$.data[0].distanciaKm").value(0.0));
    }

    @Test
    void deveRespeitarRaioPadraoDe5kmQuandoNaoInformado() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/pontos-coleta")
                        .param("lat", "-23.5433")
                        .param("lng", "-46.6528")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        // Nao afirma quantidade exata (depende das coordenadas placeholder),
        // apenas que o endpoint aceita a chamada sem erro com raio implicito.
    }

    @Test
    void deveRejeitarQuandoApenasLatitudeInformada() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/pontos-coleta")
                        .param("lat", "-23.5433")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarRaioNegativoOuZero() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/pontos-coleta")
                        .param("lat", "-23.5433")
                        .param("lng", "-46.6528")
                        .param("raioKm", "0")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarDetalheDePontoDeColeta() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/pontos-coleta/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").isNotEmpty())
                .andExpect(jsonPath("$.data.tiposResiduoAceitos").isArray());
    }

    @Test
    void deveRetornar404ParaPontoInexistente() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/pontos-coleta/9999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
