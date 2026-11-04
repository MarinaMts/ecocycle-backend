package com.ecocycle.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Alternativa que o usuario escolheu em uma pergunta na ultima tentativa do
 * quiz. Permite montar a correcao depois, mesmo que o usuario saia do app.
 */
@Entity
@Table(name = "quiz_respostas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quiz_resposta_resultado_pergunta",
                columnNames = {"quiz_resultado_id", "pergunta_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_resultado_id", nullable = false)
    private QuizResultado quizResultado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id", nullable = false)
    private Pergunta pergunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternativa_id", nullable = false)
    private Alternativa alternativa;
}
