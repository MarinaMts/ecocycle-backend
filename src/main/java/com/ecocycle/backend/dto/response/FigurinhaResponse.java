package com.ecocycle.backend.dto.response;

import com.ecocycle.backend.entity.Figurinha;

public class FigurinhaResponse {

    private Long id;
    private String codigo;
    private String tipo;
    private Integer ordem;
    private boolean desbloqueada;

    // Os campos abaixo so sao preenchidos quando a figurinha esta desbloqueada
    // (ver documento de decisoes tecnicas, secao 4: "conteudo detalhado so apos desbloqueio")
    private String nome;
    private String descricao;

    public static FigurinhaResponse fromEntity(Figurinha figurinha, boolean desbloqueada) {
        FigurinhaResponse dto = new FigurinhaResponse();
        dto.id = figurinha.getId();
        dto.codigo = figurinha.getCodigo();
        dto.tipo = figurinha.getTipo().name();
        dto.ordem = figurinha.getOrdem();
        dto.desbloqueada = desbloqueada;
        if (desbloqueada) {
            dto.nome = figurinha.getNome();
            dto.descricao = figurinha.getDescricao();
        }
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public boolean isDesbloqueada() {
        return desbloqueada;
    }

    public void setDesbloqueada(boolean desbloqueada) {
        this.desbloqueada = desbloqueada;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
