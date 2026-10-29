package com.ecocycle.backend.seed;

import com.ecocycle.backend.enums.Trilha;

import java.util.List;

/**
 * Item do catalogo do seed: um texto educativo com o quiz e a figurinha QUIZ
 * correspondentes. A chave natural e o {@code codigo} (ex.: "EDU-01").
 */
record ConteudoSeed(
        String codigo,
        Trilha trilha,
        String titulo,
        String corpo,
        int ordem,
        String imagemSugerida,
        int xpQuiz,
        List<PerguntaSeed> perguntas,
        FigurinhaQuizSeed figurinha
) {
}
