package com.ecocycle.backend.enums;

public enum Trilha {
    OS_4RS("Os 4Rs"),
    LIXO_ELETRONICO("Lixo Eletrônico");

    private final String nomeExibicao;

    Trilha(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }
}
