package com.ecocycle.backend.seed;

import com.ecocycle.backend.entity.Alternativa;
import com.ecocycle.backend.entity.Pergunta;
import com.ecocycle.backend.entity.Quiz;

import java.util.List;

/**
 * Helper interno do seed: converte a estrutura compacta
 * {enunciado, String[]{alt_a, alt_b, alt_c, alt_d}, indiceCorreta}
 * em entidades Pergunta + Alternativa persistidas via cascade do Quiz.
 *
 * Nao e um @Component - e usado apenas internamente pelo DataSeeder,
 * dentro da mesma transacao de inicializacao (CommandLineRunner).
 */
final class SeedHelper {

    private SeedHelper() {
    }

    /**
     * @param quiz          quiz ja persistido (com id) ao qual as perguntas pertencem
     * @param perguntasData lista de Object[]{String enunciado, String[4] alternativas, Integer indiceCorreta}
     * @param letras        rotulos de exibicao das alternativas, ex.: {"a","b","c","d"}
     */
    static void salvarPerguntas(Quiz quiz, List<Object[]> perguntasData, String[] letras) {
        int ordemPergunta = 1;
        for (Object[] dado : perguntasData) {
            String enunciado = (String) dado[0];
            String[] textosAlternativas = (String[]) dado[1];
            int indiceCorreta = (Integer) dado[2];

            Pergunta pergunta = Pergunta.builder()
                    .quiz(quiz)
                    .enunciado(enunciado)
                    .ordem(ordemPergunta++)
                    .build();

            for (int i = 0; i < textosAlternativas.length; i++) {
                Alternativa alternativa = Alternativa.builder()
                        .pergunta(pergunta)
                        .texto(textosAlternativas[i])
                        .letra(letras[i])
                        .correta(i == indiceCorreta)
                        .build();
                pergunta.getAlternativas().add(alternativa);
            }

            quiz.getPerguntas().add(pergunta);
        }
    }
}
