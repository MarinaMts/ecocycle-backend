package com.ecocycle.backend.dto.response;

import java.util.List;

public class TrilhaResponse {

    private String trilha;
    private String nomeExibicao;
    private List<ConteudoResumoResponse> conteudos;

    public TrilhaResponse() {
    }

    public TrilhaResponse(String trilha, String nomeExibicao, List<ConteudoResumoResponse> conteudos) {
        this.trilha = trilha;
        this.nomeExibicao = nomeExibicao;
        this.conteudos = conteudos;
    }

    public String getTrilha() {
        return trilha;
    }

    public void setTrilha(String trilha) {
        this.trilha = trilha;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public void setNomeExibicao(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public List<ConteudoResumoResponse> getConteudos() {
        return conteudos;
    }

    public void setConteudos(List<ConteudoResumoResponse> conteudos) {
        this.conteudos = conteudos;
    }
}
