package com.ecocycle.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Quiz de fixacao de um texto educativo. Relacao 1:1 com ConteudoEducativo
 * (1 texto -> 1 quiz do mesmo tema, ver documento de decisoes tecnicas, secao 3).
 * Sem nota minima: participar ja libera a figurinha correspondente.
 */
@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_educativo_id", nullable = false, unique = true)
    private ConteudoEducativo conteudoEducativo;

    // XP concedido na primeira conclusao (refazer o quiz nao gera XP extra)
    @Column(nullable = false)
    @Builder.Default
    private Integer xpRecompensa = 10;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Pergunta> perguntas = new ArrayList<>();
}
