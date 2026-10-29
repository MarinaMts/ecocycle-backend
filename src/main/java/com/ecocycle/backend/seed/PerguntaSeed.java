package com.ecocycle.backend.seed;

import java.util.List;

/**
 * Pergunta de quiz do catalogo. A ordem da pergunta no quiz e a posicao dela
 * na lista (1-based) e as alternativas recebem as letras a, b, c, d na ordem
 * em que aparecem. {@code indiceCorreta} e 0-based.
 */
record PerguntaSeed(String enunciado, List<String> alternativas, int indiceCorreta) {
}
