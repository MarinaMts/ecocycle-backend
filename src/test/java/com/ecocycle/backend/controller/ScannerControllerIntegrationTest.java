package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import com.ecocycle.backend.entity.LeituraScanner;
import com.ecocycle.backend.repository.LeituraScannerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScannerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LeituraScannerRepository leituraScannerRepository;

    @Test
    void deveReconhecerComponenteComConfiancaAcimaDoLimiarEDesbloquearFigurinha() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "MOUSE", "confianca": 95.0}
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

    @ParameterizedTest
    @CsvSource({"FONTE, Fonte de alimentação", "PENDRIVE, Pendrive"})
    void deveDesbloquearFigurinhasDosComponentesNovosDoModelo(String identificador, String nome) throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "%s", "confianca": 95.0}
                """.formatted(identificador);

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconhecido").value(true))
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(true))
                .andExpect(jsonPath("$.data.figurinha.nome").value(nome));
    }

    @Test
    void deveTratarComoNaoReconhecidoQuandoConfiancaAbaixoDoLimiar() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "MOUSE", "confianca": 45.0}
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
    void deveTratarComoNaoReconhecidoLogoAbaixoDoLimiarDe90() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "MOUSE", "confianca": 89.9}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconhecido").value(false))
                .andExpect(jsonPath("$.data.figurinha").doesNotExist());
    }

    @Test
    void naoDeveDesbloquearNovamenteFigurinhaJaObtida() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "PILHA", "confianca": 90.0}
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRegistrarLeituraOutrosSemDesbloquearFigurinha() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "OUTROS", "confianca": 99.0}
                """;

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconhecido").value(false))
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(false))
                .andExpect(jsonPath("$.data.figurinha").doesNotExist());

        LeituraScanner leitura = leiturasDe("OUTROS").get(0);
        assertThat(leitura.getConfianca()).isEqualTo(99.0);
        assertThat(leitura.isReconhecido()).isFalse();
        assertThat(leitura.isManual()).isFalse();
    }

    // Sem a transacao do teste: e o commit real do service que precisa sobreviver ao 400.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveManterLeituraDeIdentificadorDesconhecidoMesmoRespondendo400() throws Exception {
        String identificador = "DESCONHECIDO_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "%s", "confianca": 90.0}
                """.formatted(identificador);

        try {
            mockMvc.perform(post("/api/v1/scanner/reconhecer")
                            .header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());

            assertThat(leiturasDe(identificador)).hasSize(1);
        } finally {
            leituraScannerRepository.deleteAll(leiturasDe(identificador));
        }
    }

    @Test
    void deveRejeitarIdentificadorMaiorQueOLimiteDaColuna() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "%s", "confianca": 90.0}
                """.formatted("X".repeat(101));

        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    private List<LeituraScanner> leiturasDe(String identificador) {
        return leituraScannerRepository.findAll().stream()
                .filter(l -> identificador.equals(l.getIdentificador()))
                .toList();
    }

    @Test
    void deveRejeitarConfiancaForaDoIntervaloValido() throws Exception {
        String token = registrarERetornarToken();

        String payload = """
                {"identificadorComponente": "FERRO_PASSAR", "confianca": 150.0}
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
