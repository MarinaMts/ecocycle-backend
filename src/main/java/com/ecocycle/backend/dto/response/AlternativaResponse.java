package com.ecocycle.backend.dto.response;

import com.ecocycle.backend.entity.Alternativa;

/**
 * Nunca inclui o campo "correta" - o app so deve saber o resultado
 * apos submeter as respostas.
 */
public class AlternativaResponse {

    private Long id;
    private String letra;
    private String texto;

    public AlternativaResponse() {
    }

    public static AlternativaResponse fromEntity(Alternativa alternativa) {
        AlternativaResponse dto = new AlternativaResponse();
        dto.id = alternativa.getId();
        dto.letra = alternativa.getLetra();
        dto.texto = alternativa.getTexto();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
