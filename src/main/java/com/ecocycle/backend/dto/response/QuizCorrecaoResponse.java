package com.ecocycle.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Correcao da ultima tentativa do usuario em um quiz
 * (GET /quizzes/{id}/correcao).
 */
public class QuizCorrecaoResponse {

    private Long quizId;
    private Integer acertos;
    private Integer totalPerguntas;
    private LocalDateTime respondidoEm;
    private List<CorrecaoPerguntaResponse> correcao;

    public QuizCorrecaoResponse() {
    }

    public QuizCorrecaoResponse(
            Long quizId, Integer acertos, Integer totalPerguntas,
            LocalDateTime respondidoEm, List<CorrecaoPerguntaResponse> correcao
    ) {
        this.quizId = quizId;
        this.acertos = acertos;
        this.totalPerguntas = totalPerguntas;
        this.respondidoEm = respondidoEm;
        this.correcao = correcao;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Integer getAcertos() {
        return acertos;
    }

    public void setAcertos(Integer acertos) {
        this.acertos = acertos;
    }

    public Integer getTotalPerguntas() {
        return totalPerguntas;
    }

    public void setTotalPerguntas(Integer totalPerguntas) {
        this.totalPerguntas = totalPerguntas;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public void setRespondidoEm(LocalDateTime respondidoEm) {
        this.respondidoEm = respondidoEm;
    }

    public List<CorrecaoPerguntaResponse> getCorrecao() {
        return correcao;
    }

    public void setCorrecao(List<CorrecaoPerguntaResponse> correcao) {
        this.correcao = correcao;
    }
}
