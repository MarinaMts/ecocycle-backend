package com.ecocycle.backend.dto.response;

public class ProgressoResponse {

    private Integer xpTotal;
    private long conteudosLidos;
    private long quizzesConcluidos;
    private long figurinhasDesbloqueadas;
    private long totalFigurinhas;

    public ProgressoResponse() {
    }

    public ProgressoResponse(
            Integer xpTotal, long conteudosLidos, long quizzesConcluidos,
            long figurinhasDesbloqueadas, long totalFigurinhas
    ) {
        this.xpTotal = xpTotal;
        this.conteudosLidos = conteudosLidos;
        this.quizzesConcluidos = quizzesConcluidos;
        this.figurinhasDesbloqueadas = figurinhasDesbloqueadas;
        this.totalFigurinhas = totalFigurinhas;
    }

    public Integer getXpTotal() {
        return xpTotal;
    }

    public void setXpTotal(Integer xpTotal) {
        this.xpTotal = xpTotal;
    }

    public long getConteudosLidos() {
        return conteudosLidos;
    }

    public void setConteudosLidos(long conteudosLidos) {
        this.conteudosLidos = conteudosLidos;
    }

    public long getQuizzesConcluidos() {
        return quizzesConcluidos;
    }

    public void setQuizzesConcluidos(long quizzesConcluidos) {
        this.quizzesConcluidos = quizzesConcluidos;
    }

    public long getFigurinhasDesbloqueadas() {
        return figurinhasDesbloqueadas;
    }

    public void setFigurinhasDesbloqueadas(long figurinhasDesbloqueadas) {
        this.figurinhasDesbloqueadas = figurinhasDesbloqueadas;
    }

    public long getTotalFigurinhas() {
        return totalFigurinhas;
    }

    public void setTotalFigurinhas(long totalFigurinhas) {
        this.totalFigurinhas = totalFigurinhas;
    }
}
