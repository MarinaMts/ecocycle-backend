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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuizControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PerguntaRepository perguntaRepository;

    // Quiz do conteudo EDU-01 (Reciclar) - primeiro conteudo/quiz seedado, id=1 de forma estavel
    private static final Long QUIZ_ID_EDU01 = 1L;

    @Test
    void deveRetornarQuizSemExporGabarito() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(get("/api/v1/quizzes/por-conteudo/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.perguntas.length()").value(5))
                .andExpect(jsonPath("$.data.perguntas[0].alternativas.length()").value(4))
                // o campo "correta" nunca deve aparecer na resposta ao usuario
                .andExpect(jsonPath("$.data.perguntas[0].alternativas[0].correta").doesNotExist());
    }

    @Test
    void deveConcluirQuizComTodasRespostasCorretasEGanharXpEFigurinha() throws Exception {
        String token = registrarERetornarToken();
        String payload = montarPayloadComRespostas(QUIZ_ID_EDU01, true);

        mockMvc.perform(post("/api/v1/quizzes/" + QUIZ_ID_EDU01 + "/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acertos").value(5))
                .andExpect(jsonPath("$.data.totalPerguntas").value(5))
                .andExpect(jsonPath("$.data.primeiraConclusao").value(true))
                .andExpect(jsonPath("$.data.xpGanho").value(10))
                .andExpect(jsonPath("$.data.xpTotalAtual").value(10))
                .andExpect(jsonPath("$.data.figurinhaDesbloqueada.codigo").value("FIG-01"))
                .andExpect(jsonPath("$.data.figurinhaDesbloqueada.desbloqueada").value(true));
    }

    @Test
    void refazerQuizNaoDeveGerarXpNemFigurinhaAdicional() throws Exception {
        String token = registrarERetornarToken();
        String payload = montarPayloadComRespostas(QUIZ_ID_EDU01, true);

        // primeira vez
        mockMvc.perform(post("/api/v1/quizzes/" + QUIZ_ID_EDU01 + "/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.xpGanho").value(10));

        // segunda vez - mesmo com acerto total, nao deve gerar XP nem figurinha novamente
        mockMvc.perform(post("/api/v1/quizzes/" + QUIZ_ID_EDU01 + "/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acertos").value(5))
                .andExpect(jsonPath("$.data.primeiraConclusao").value(false))
                .andExpect(jsonPath("$.data.xpGanho").value(0))
                .andExpect(jsonPath("$.data.xpTotalAtual").value(10))
                .andExpect(jsonPath("$.data.figurinhaDesbloqueada").doesNotExist());
    }

    @Test
    void deveAceitarQuizComRespostasErradasSemNotaMinima() throws Exception {
        String token = registrarERetornarToken();
        // sem nota minima: mesmo errando algumas, ja libera XP e figurinha na 1a vez
        String payload = montarPayloadComRespostas(QUIZ_ID_EDU01, false);

        mockMvc.perform(post("/api/v1/quizzes/" + QUIZ_ID_EDU01 + "/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acertos").value(0))
                .andExpect(jsonPath("$.data.primeiraConclusao").value(true))
                .andExpect(jsonPath("$.data.xpGanho").value(10))
                .andExpect(jsonPath("$.data.figurinhaDesbloqueada.codigo").value("FIG-01"));
    }

    @Test
    void deveRejeitarSubmissaoComPerguntasFaltando() throws Exception {
        String token = registrarERetornarToken();

        List<Pergunta> perguntas = perguntaRepository.findByQuizIdComAlternativas(QUIZ_ID_EDU01);

        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode respostas = root.putArray("respostas");
        // manda so as 3 primeiras das 5 perguntas
        for (int i = 0; i < 3; i++) {
            Pergunta pergunta = perguntas.get(i);
            Alternativa qualquerAlternativa = pergunta.getAlternativas().get(0);
            ObjectNode item = respostas.addObject();
            item.put("perguntaId", pergunta.getId());
            item.put("alternativaId", qualquerAlternativa.getId());
        }

        mockMvc.perform(post("/api/v1/quizzes/" + QUIZ_ID_EDU01 + "/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(root)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarSubmissaoParaQuizInexistente() throws Exception {
        String token = registrarERetornarToken();

        // A lista de respostas precisa ser nao-vazia para passar da validacao de
        // Bean Validation (@NotEmpty) e so entao chegar na busca do quiz em si,
        // que e onde o 404 realmente deve ocorrer.
        String payload = """
                {"respostas": [{"perguntaId": 1, "alternativaId": 1}]}
                """;

        mockMvc.perform(post("/api/v1/quizzes/9999/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    /**
     * Monta o payload de submissao usando o gabarito real do banco (via repository),
     * em vez de IDs fixos - torna o teste resiliente a qualquer reordenacao do seed.
     */
    private String montarPayloadComRespostas(Long quizId, boolean todasCorretas) throws Exception {
        List<Pergunta> perguntas = perguntaRepository.findByQuizIdComAlternativas(quizId);
        assertThat(perguntas).hasSize(5);

        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode respostas = root.putArray("respostas");

        for (Pergunta pergunta : perguntas) {
            Alternativa escolhida = todasCorretas
                    ? pergunta.getAlternativas().stream().filter(Alternativa::getCorreta).findFirst().orElseThrow()
                    : pergunta.getAlternativas().stream().filter(a -> !a.getCorreta()).findFirst().orElseThrow();

            ObjectNode item = respostas.addObject();
            item.put("perguntaId", pergunta.getId());
            item.put("alternativaId", escolhida.getId());
        }

        return objectMapper.writeValueAsString(root);
    }
}
