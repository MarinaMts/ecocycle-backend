package com.ecocycle.backend.dto.response;

import com.ecocycle.backend.entity.Pergunta;

import java.util.List;

public class PerguntaResponse {

    private Long id;
    private String enunciado;
    private Integer ordem;
    private List<AlternativaResponse> alternativas;

    public PerguntaResponse() {
    }

    public static PerguntaResponse fromEntity(Pergunta pergunta) {
        PerguntaResponse dto = new PerguntaResponse();
        dto.id = pergunta.getId();
        dto.enunciado = pergunta.getEnunciado();
        dto.ordem = pergunta.getOrdem();
        dto.alternativas = pergunta.getAlternativas().stream()
                .map(AlternativaResponse::fromEntity)
                .toList();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public List<AlternativaResponse> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<AlternativaResponse> alternativas) {
        this.alternativas = alternativas;
    }
}
