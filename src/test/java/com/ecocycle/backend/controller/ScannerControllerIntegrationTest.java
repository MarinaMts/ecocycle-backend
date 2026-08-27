package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScannerControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void deveReconhecerComponenteComConfiancaAcimaDoLimiarEDesbloquearFigurinha() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "BATERIA_LITIO", "confianca": 87.5}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconhecido").value(true))
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(true))
                .andExpect(jsonPath("$.data.figurinha.identificadorScan").doesNotExist()) // dto nao expoe esse campo
                .andExpect(jsonPath("$.data.figurinha.desbloqueada").value(true))
                .andExpect(jsonPath("$.data.figurinha.nome").isNotEmpty());
    }

    @Test
    void deveTratarComoNaoReconhecidoQuandoConfiancaAbaixoDoLimiar() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "BATERIA_LITIO", "confianca": 45.0}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconhecido").value(false))
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(false))
                .andExpect(jsonPath("$.data.figurinha").doesNotExist());
    }

    @Test
    void naoDeveDesbloquearNovamenteFigurinhaJaObtida() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "PLACA_MAE", "confianca": 90.0}
                """;

        // primeiro scan - desbloqueia
        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(true));

        // segundo scan do MESMO componente - reconhece de novo, mas nao desbloqueia de novo
        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconhecido").value(true))
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(false))
                // mas a info da figurinha continua sendo reexibida
                .andExpect(jsonPath("$.data.figurinha.nome").isNotEmpty());
    }

    @Test
    void deveRejeitarIdentificadorDeComponenteDesconhecido() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "COMPONENTE_INEXISTENTE", "confianca": 90.0}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarConfiancaForaDoIntervaloValido() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "CABO_USB", "confianca": 150.0}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarRequisicaoSemIdentificador() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"confianca": 90.0}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
