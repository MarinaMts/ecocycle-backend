package com.ecocycle.backend.dto.response;

import com.ecocycle.backend.entity.ConteudoEducativo;

public class ConteudoDetalheResponse {

    private Long id;
    private String codigo;
    private String trilha;
    private String titulo;
    private String corpo;
    private Integer ordem;
    private String imagemSugerida;
    private boolean lido;
    private boolean quizConcluido;
    private Long quizId;

    public ConteudoDetalheResponse() {
    }

    public static ConteudoDetalheResponse fromEntity(
            ConteudoEducativo conteudo, boolean lido, boolean quizConcluido, Long quizId
    ) {
        ConteudoDetalheResponse dto = new ConteudoDetalheResponse();
        dto.id = conteudo.getId();
        dto.codigo = conteudo.getCodigo();
        dto.trilha = conteudo.getTrilha().name();
        dto.titulo = conteudo.getTitulo();
        dto.corpo = conteudo.getCorpo();
        dto.ordem = conteudo.getOrdem();
        dto.imagemSugerida = conteudo.getImagemSugerida();
        dto.lido = lido;
        dto.quizConcluido = quizConcluido;
        dto.quizId = quizId;
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTrilha() {
        return trilha;
    }

    public void setTrilha(String trilha) {
        this.trilha = trilha;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getImagemSugerida() {
        return imagemSugerida;
    }

    public void setImagemSugerida(String imagemSugerida) {
        this.imagemSugerida = imagemSugerida;
    }

    public boolean isLido() {
        return lido;
    }

    public void setLido(boolean lido) {
        this.lido = lido;
    }

    public boolean isQuizConcluido() {
        return quizConcluido;
    }

    public void setQuizConcluido(boolean quizConcluido) {
        this.quizConcluido = quizConcluido;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }
}
