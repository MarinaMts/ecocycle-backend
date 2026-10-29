package com.ecocycle.backend.seed;

/**
 * Figurinha do tipo SCAN, desbloqueada pelo scanner. O {@code identificadorScan}
 * precisa bater com a classe reconhecida pelo modelo on-device.
 * A chave natural e o {@code codigo}.
 */
record FigurinhaScanSeed(String codigo, String nome, String identificadorScan, String descricao, int ordem) {
}
