package com.ecocycle.backend.dto.response;

/**
 * Correcao de uma pergunta: o que o usuario marcou e qual era a alternativa
 * correta. So e enviada depois que o usuario responde o quiz.
 */
public class CorrecaoPerguntaResponse {

    private Long perguntaId;
    private Integer ordem;
    private Long alternativaEscolhidaId;
    private Long alternativaCorretaId;
    private boolean acertou;

    public CorrecaoPerguntaResponse() {
    }

    public CorrecaoPerguntaResponse(
            Long perguntaId, Integer ordem, Long alternativaEscolhidaId, Long alternativaCorretaId
    ) {
        this.perguntaId = perguntaId;
        this.ordem = ordem;
        this.alternativaEscolhidaId = alternativaEscolhidaId;
        this.alternativaCorretaId = alternativaCorretaId;
        this.acertou = alternativaCorretaId.equals(alternativaEscolhidaId);
    }

    public Long getPerguntaId() {
        return perguntaId;
    }

    public void setPerguntaId(Long perguntaId) {
        this.perguntaId = perguntaId;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Long getAlternativaEscolhidaId() {
        return alternativaEscolhidaId;
    }

    public void setAlternativaEscolhidaId(Long alternativaEscolhidaId) {
        this.alternativaEscolhidaId = alternativaEscolhidaId;
    }

    public Long getAlternativaCorretaId() {
        return alternativaCorretaId;
    }

    public void setAlternativaCorretaId(Long alternativaCorretaId) {
        this.alternativaCorretaId = alternativaCorretaId;
    }

    public boolean isAcertou() {
        return acertou;
    }

    public void setAcertou(boolean acertou) {
        this.acertou = acertou;
    }
}
