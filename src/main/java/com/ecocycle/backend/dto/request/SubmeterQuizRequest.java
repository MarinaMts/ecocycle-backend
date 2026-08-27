package com.ecocycle.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SubmeterQuizRequest {

    @NotEmpty(message = "A lista de respostas nao pode ser vazia")
    @Valid
    private List<RespostaItem> respostas;

    public List<RespostaItem> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaItem> respostas) {
        this.respostas = respostas;
    }

    public static class RespostaItem {

        @NotNull(message = "O id da pergunta e obrigatorio")
        private Long perguntaId;

        @NotNull(message = "O id da alternativa escolhida e obrigatorio")
        private Long alternativaId;

        public Long getPerguntaId() {
            return perguntaId;
        }

        public void setPerguntaId(Long perguntaId) {
            this.perguntaId = perguntaId;
        }

        public Long getAlternativaId() {
            return alternativaId;
        }

        public void setAlternativaId(Long alternativaId) {
            this.alternativaId = alternativaId;
        }
    }
}
