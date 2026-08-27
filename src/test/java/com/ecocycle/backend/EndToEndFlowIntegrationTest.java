package com.ecocycle.backend;

import com.ecocycle.backend.entity.Alternativa;
import com.ecocycle.backend.entity.Pergunta;
import com.ecocycle.backend.repository.PerguntaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reproduz, de forma automatizada, o roteiro de validacao manual que foi
 * seguido no Postman ao longo das Semanas 1 a 3: cadastro -> autenticacao ->
 * leitura de conteudo -> quiz -> figurinha -> progresso -> logistica -> scanner.
 *
 * Este teste existe para dar confianca de que os modulos continuam
 * funcionando juntos corretamente, nao apenas isoladamente.
 */
class EndToEndFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PerguntaRepository perguntaRepository;

    @Test
    void fluxoCompletoDoUsuarioDoCadastroAoScanner() throws Exception {
        String email = emailAleatorio();
        String apelido = apelidoAleatorio();
        String senha = "senha123";

        // 1. Registro
        String payloadRegister = """
                {"email": "%s", "apelido": "%s", "senha": "%s"}
                """.formatted(email, apelido, senha);

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadRegister))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // 2. Get Me confirma autenticacao
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        // 3. Listar trilhas - tudo comeca nao lido
        mockMvc.perform(get("/api/v1/trilhas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 4. Ler o conteudo EDU-01
        mockMvc.perform(post("/api/v1/conteudos/1/marcar-lido").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lido").value(true));

        // 5. Buscar o quiz correspondente
        MvcResult quizResult = mockMvc.perform(get("/api/v1/quizzes/por-conteudo/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode quizJson = objectMapper.readTree(quizResult.getResponse().getContentAsString());
        long quizId = quizJson.get("data").get("id").asLong();

        // 6. Submeter respostas corretas (gabarito buscado via repository)
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

        mockMvc.perform(post("/api/v1/quizzes/" + quizId + "/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(root)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acertos").value(5))
                .andExpect(jsonPath("$.data.xpGanho").value(10))
                .andExpect(jsonPath("$.data.figurinhaDesbloqueada.codigo").value("FIG-01"));

        // 7. Album reflete a figurinha desbloqueada
        mockMvc.perform(get("/api/v1/figurinhas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].desbloqueada").value(true));

        // 8. Progresso consolidado bate com o que foi feito ate aqui
        mockMvc.perform(get("/api/v1/progresso/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.xpTotal").value(10))
                .andExpect(jsonPath("$.data.conteudosLidos").value(1))
                .andExpect(jsonPath("$.data.quizzesConcluidos").value(1))
                .andExpect(jsonPath("$.data.figurinhasDesbloqueadas").value(1));

        // 9. Logistica - busca pontos de coleta proximos ao Mackenzie
        mockMvc.perform(get("/api/v1/pontos-coleta")
                        .param("lat", "-23.5433")
                        .param("lng", "-46.6528")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 10. Scanner - reconhece um componente e ganha outra figurinha
        String payloadScan = """
                {"identificadorComponente": "CABO_USB", "confianca": 75.0}
                """;
        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadScan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.novaDesbloqueada").value(true));

        // 11. Progresso final reflete as duas figurinhas (1 quiz + 1 scan)
        MvcResult progressoFinal = mockMvc.perform(get("/api/v1/progresso/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode progressoJson = objectMapper.readTree(progressoFinal.getResponse().getContentAsString());
        assertThat(progressoJson.get("data").get("figurinhasDesbloqueadas").asInt()).isEqualTo(2);

        // 12. Atualiza avatar
        mockMvc.perform(patch("/api/v1/users/me/avatar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\": \"AVATAR_03\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatar").value("AVATAR_03"));

        // 13. Desativa a conta ao final da jornada
        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 14. Login apos desativacao deve falhar
        String payloadLogin = """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadLogin))
                .andExpect(status().isUnauthorized());
    }
}
