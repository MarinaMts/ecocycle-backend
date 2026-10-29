package com.ecocycle.backend.seed;

/**
 * Figurinha do tipo QUIZ, desbloqueada ao concluir o quiz do conteudo
 * ao qual pertence. A chave natural e o {@code codigo} (ex.: "FIG-01").
 */
record FigurinhaQuizSeed(String codigo, String nome, String descricao, int ordem) {
}
