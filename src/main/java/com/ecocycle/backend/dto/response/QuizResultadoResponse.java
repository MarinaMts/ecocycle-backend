package com.ecocycle.backend.dto.response;

public class QuizResultadoResponse {

    private Integer acertos;
    private Integer totalPerguntas;
    private boolean primeiraConclusao;
    private Integer xpGanho;
    private Integer xpTotalAtual;
    private FigurinhaResponse figurinhaDesbloqueada;

    public QuizResultadoResponse() {
    }

    public QuizResultadoResponse(
            Integer acertos, Integer totalPerguntas, boolean primeiraConclusao,
            Integer xpGanho, Integer xpTotalAtual, FigurinhaResponse figurinhaDesbloqueada
    ) {
        this.acertos = acertos;
        this.totalPerguntas = totalPerguntas;
        this.primeiraConclusao = primeiraConclusao;
        this.xpGanho = xpGanho;
        this.xpTotalAtual = xpTotalAtual;
        this.figurinhaDesbloqueada = figurinhaDesbloqueada;
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

    public boolean isPrimeiraConclusao() {
        return primeiraConclusao;
    }

    public void setPrimeiraConclusao(boolean primeiraConclusao) {
        this.primeiraConclusao = primeiraConclusao;
    }

    public Integer getXpGanho() {
        return xpGanho;
    }

    public void setXpGanho(Integer xpGanho) {
        this.xpGanho = xpGanho;
    }

    public Integer getXpTotalAtual() {
        return xpTotalAtual;
    }

    public void setXpTotalAtual(Integer xpTotalAtual) {
        this.xpTotalAtual = xpTotalAtual;
    }

    public FigurinhaResponse getFigurinhaDesbloqueada() {
        return figurinhaDesbloqueada;
    }

    public void setFigurinhaDesbloqueada(FigurinhaResponse figurinhaDesbloqueada) {
        this.figurinhaDesbloqueada = figurinhaDesbloqueada;
    }
}
