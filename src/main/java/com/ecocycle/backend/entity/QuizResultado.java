package com.ecocycle.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Resultado consolidado do usuario em um quiz - apenas total de acertos/erros
 * da tentativa mais recente, sem historico por tentativa (ver documento de
 * decisoes tecnicas, secao 3). XP e concedido apenas na primeira conclusao.
 */
@Entity
@Table(name = "quiz_resultados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quiz_resultado_user_quiz", columnNames = {"user_id", "quiz_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private Integer acertos;

    @Column(nullable = false)
    private Integer totalPerguntas;

    // XP so e concedido true na primeira vez que o quiz e concluido
    @Column(nullable = false)
    @Builder.Default
    private Boolean xpConcedido = false;

    @Column(name = "primeira_conclusao_em", nullable = false)
    private LocalDateTime primeiraConclusaoEm;

    @Column(name = "ultima_tentativa_em", nullable = false)
    private LocalDateTime ultimaTentativaEm;

    @PrePersist
    protected void aoPersistir() {
        LocalDateTime agora = LocalDateTime.now();
        if (this.primeiraConclusaoEm == null) {
            this.primeiraConclusaoEm = agora;
        }
        this.ultimaTentativaEm = agora;
    }
}
