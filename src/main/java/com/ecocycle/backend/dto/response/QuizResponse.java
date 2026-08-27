package com.ecocycle.backend.dto.response;

import java.util.List;

public class QuizResponse {

    private Long id;
    private Long conteudoEducativoId;
    private String conteudoTitulo;
    private List<PerguntaResponse> perguntas;

    public QuizResponse() {
    }

    public QuizResponse(Long id, Long conteudoEducativoId, String conteudoTitulo, List<PerguntaResponse> perguntas) {
        this.id = id;
        this.conteudoEducativoId = conteudoEducativoId;
        this.conteudoTitulo = conteudoTitulo;
        this.perguntas = perguntas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConteudoEducativoId() {
        return conteudoEducativoId;
    }

    public void setConteudoEducativoId(Long conteudoEducativoId) {
        this.conteudoEducativoId = conteudoEducativoId;
    }

    public String getConteudoTitulo() {
        return conteudoTitulo;
    }

    public void setConteudoTitulo(String conteudoTitulo) {
        this.conteudoTitulo = conteudoTitulo;
    }

    public List<PerguntaResponse> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<PerguntaResponse> perguntas) {
        this.perguntas = perguntas;
    }
}
