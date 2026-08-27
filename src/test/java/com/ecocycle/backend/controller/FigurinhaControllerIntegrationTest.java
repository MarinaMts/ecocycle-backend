package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import com.ecocycle.backend.entity.Alternativa;
import com.ecocycle.backend.entity.Pergunta;
import com.ecocycle.backend.repository.PerguntaRepository;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FigurinhaControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PerguntaRepository perguntaRepository;

    @Test
    void albumDeveComecarComDezFigurinhasTodasBloqueadas() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/figurinhas")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(10))
                .andExpect(jsonPath("$.data[0].desbloqueada").value(false))
                .andExpect(jsonPath("$.data[0].nome").doesNotExist());
    }

    @Test
    void figurinhaDesbloqueadaDeveExibirNomeEDescricao_bloqueadaNao() throws Exception {
        String token = registrarERetornarToken();

        // Completa o quiz 1 (Reciclar) para desbloquear a FIG-01
        String payload = montarPayloadComRespostasCorretas(1L);
        mockMvc.perform(post("/api/v1/quizzes/1/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/figurinhas")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codigo").value("FIG-01"))
                .andExpect(jsonPath("$.data[0].desbloqueada").value(true))
                .andExpect(jsonPath("$.data[0].nome").value("Reciclador Urbano"))
                // a segunda figurinha continua bloqueada e sem nome exposto
                .andExpect(jsonPath("$.data[1].desbloqueada").value(false))
                .andExpect(jsonPath("$.data[1].nome").doesNotExist());
    }

    @Test
    void deveBuscarDetalheDeFigurinhaPorId() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/figurinhas/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codigo").value("FIG-01"))
                .andExpect(jsonPath("$.data.desbloqueada").value(false));
    }

    @Test
    void deveRetornar404ParaFigurinhaInexistente() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/figurinhas/9999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private String montarPayloadComRespostasCorretas(Long quizId) throws Exception {
        List<Pergunta> perguntas = perguntaRepository.findByQuizIdComAlternativas(quizId);
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode respostas = root.putArray("respostas");
        for (Pergunta pergunta : perguntas) {
            Alternativa correta = pergunta.getAlternativas().stream()
                    .filter(Alternativa::getCorreta).findFirst().orElseThrow();
            ObjectNode item = respostas.addObject();
            item.put("perguntaId", pergunta.getId());
            item.put("alternativaId", correta.getId());
        }
        return objectMapper.writeValueAsString(root);
    }
}
