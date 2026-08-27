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

class ProgressoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PerguntaRepository perguntaRepository;

    @Test
    void progressoDeveComecarZerado() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/progresso/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.xpTotal").value(0))
                .andExpect(jsonPath("$.data.conteudosLidos").value(0))
                .andExpect(jsonPath("$.data.quizzesConcluidos").value(0))
                .andExpect(jsonPath("$.data.figurinhasDesbloqueadas").value(0))
                .andExpect(jsonPath("$.data.totalFigurinhas").value(10));
    }

    @Test
    void progressoDeveRefletirLeituraEQuizConcluido() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(post("/api/v1/conteudos/1/marcar-lido")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        String payload = montarPayloadComRespostasCorretas(1L);
        mockMvc.perform(post("/api/v1/quizzes/1/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/progresso/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.xpTotal").value(10))
                .andExpect(jsonPath("$.data.conteudosLidos").value(1))
                .andExpect(jsonPath("$.data.quizzesConcluidos").value(1))
                .andExpect(jsonPath("$.data.figurinhasDesbloqueadas").value(1));
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
