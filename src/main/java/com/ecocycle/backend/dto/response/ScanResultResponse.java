package com.ecocycle.backend.dto.response;

public class ScanResultResponse {

    private boolean reconhecido;
    private String identificadorComponente;
    private Double confianca;

    // true somente quando esta chamada desbloqueou a figurinha pela primeira vez
    private boolean novaDesbloqueada;

    // Sempre preenchida quando reconhecido = true, mesmo que ja estivesse
    // desbloqueada antes (figurinha repetida so reexibe a info - ver documento
    // de decisoes tecnicas, secao 4)
    private FigurinhaResponse figurinha;

    private String mensagem;

    public ScanResultResponse() {
    }

    public ScanResultResponse(
            boolean reconhecido, String identificadorComponente, Double confianca,
            boolean novaDesbloqueada, FigurinhaResponse figurinha, String mensagem
    ) {
        this.reconhecido = reconhecido;
        this.identificadorComponente = identificadorComponente;
        this.confianca = confianca;
        this.novaDesbloqueada = novaDesbloqueada;
        this.figurinha = figurinha;
        this.mensagem = mensagem;
    }

    public boolean isReconhecido() {
        return reconhecido;
    }

    public void setReconhecido(boolean reconhecido) {
        this.reconhecido = reconhecido;
    }

    public String getIdentificadorComponente() {
        return identificadorComponente;
    }

    public void setIdentificadorComponente(String identificadorComponente) {
        this.identificadorComponente = identificadorComponente;
    }

    public Double getConfianca() {
        return confianca;
    }

    public void setConfianca(Double confianca) {
        this.confianca = confianca;
    }

    public boolean isNovaDesbloqueada() {
        return novaDesbloqueada;
    }

    public void setNovaDesbloqueada(boolean novaDesbloqueada) {
        this.novaDesbloqueada = novaDesbloqueada;
    }

    public FigurinhaResponse getFigurinha() {
        return figurinha;
    }

    public void setFigurinha(FigurinhaResponse figurinha) {
        this.figurinha = figurinha;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
