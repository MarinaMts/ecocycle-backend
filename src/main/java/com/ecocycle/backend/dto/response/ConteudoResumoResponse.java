package com.ecocycle.backend.dto.response;

public class ConteudoResumoResponse {

    private Long id;
    private String codigo;
    private String titulo;
    private Integer ordem;
    private boolean lido;
    private boolean quizConcluido;

    public ConteudoResumoResponse() {
    }

    public ConteudoResumoResponse(Long id, String codigo, String titulo, Integer ordem, boolean lido, boolean quizConcluido) {
        this.id = id;
        this.codigo = codigo;
        this.titulo = titulo;
        this.ordem = ordem;
        this.lido = lido;
        this.quizConcluido = quizConcluido;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
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
}
