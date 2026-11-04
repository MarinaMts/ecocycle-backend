package com.ecocycle.backend.controller;

import com.ecocycle.backend.BaseIntegrationTest;
import com.ecocycle.backend.entity.Alternativa;
import com.ecocycle.backend.entity.Pergunta;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.repository.ConteudoLidoRepository;
import com.ecocycle.backend.repository.FigurinhaUsuarioRepository;
import com.ecocycle.backend.repository.LeituraScannerRepository;
import com.ecocycle.backend.repository.PerguntaRepository;
import com.ecocycle.backend.repository.QuizResultadoRepository;
import com.ecocycle.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private PerguntaRepository perguntaRepository;
    @Autowired private QuizResultadoRepository quizResultadoRepository;
    @Autowired private ConteudoLidoRepository conteudoLidoRepository;
    @Autowired private FigurinhaUsuarioRepository figurinhaUsuarioRepository;
    @Autowired private LeituraScannerRepository leituraScannerRepository;

    @Test
    void excluirContaApagaOUsuarioETodosOsDadosDele() throws Exception {
        String email = emailAleatorio();
        String apelido = apelidoAleatorio();
        String token = registrarERetornarToken(email, apelido, "senha123");

        // gera dados em todas as tabelas ligadas ao usuario
        marcarConteudoComoLido(token, 1L);
        mockMvc.perform(post("/api/v1/quizzes/1/submeter")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respostasDoQuiz1()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/scanner/reconhecer")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificadorComponente": "MOUSE", "confianca": 95.0}
                                """))
                .andExpect(status().isOk());

        Long userId = userRepository.findByEmail(email).map(User::getId).orElseThrow();
        assertThat(quizResultadoRepository.findByUser_Id(userId)).isNotEmpty();
        assertThat(leituraScannerRepository.findByUsuario_Id(userId)).isNotEmpty();

        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(quizResultadoRepository.findByUser_Id(userId)).isEmpty();
        assertThat(conteudoLidoRepository.findByUser_Id(userId)).isEmpty();
        assertThat(figurinhaUsuarioRepository.findByUser_Id(userId)).isEmpty();
        assertThat(leituraScannerRepository.findByUsuario_Id(userId)).isEmpty();

        // o token antigo deixa de valer
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());

        // e-mail e apelido ficam livres para um novo cadastro
        registrarERetornarToken(email, apelido, "outraSenha1");
    }

    private String respostasDoQuiz1() throws Exception {
        List<Pergunta> perguntas = perguntaRepository.findByQuizIdComAlternativas(1L);
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode respostas = root.putArray("respostas");
        for (Pergunta pergunta : perguntas) {
            Alternativa qualquer = pergunta.getAlternativas().get(0);
            ObjectNode item = respostas.addObject();
            item.put("perguntaId", pergunta.getId());
            item.put("alternativaId", qualquer.getId());
        }
        return objectMapper.writeValueAsString(root);
    }

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
    void deveExcluirContaComSucesso() throws Exception {
        String token = registrarERetornarToken();

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conta e dados excluidos definitivamente."));
    }
}
