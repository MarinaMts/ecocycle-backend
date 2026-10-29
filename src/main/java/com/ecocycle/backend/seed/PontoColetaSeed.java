package com.ecocycle.backend.seed;

import java.util.List;

/**
 * Ponto de coleta do catalogo. A chave natural e o {@code nome}: mudar o nome
 * de um ponto ja existente cria um ponto novo (o antigo continua no banco).
 */
record PontoColetaSeed(
        String nome,
        String endereco,
        double latitude,
        double longitude,
        List<String> tiposResiduo,
        String horario
) {
}
