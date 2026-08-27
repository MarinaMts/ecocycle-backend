package com.ecocycle.backend.entity;

import com.ecocycle.backend.enums.Trilha;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Texto educativo de uma trilha (M-Learning).
 * Conteudo e seed fixo no banco (sem painel de administracao nesta fase) -
 * ver documento de decisoes tecnicas, secao 2.
 */
@Entity
@Table(name = "conteudos_educativos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conteudo_codigo", columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConteudoEducativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo estavel do conteudo, ex.: "EDU-01" (usado no seed e em referencias)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Trilha trilha;

    @Column(nullable = false, length = 150)
    private String titulo;

    // Corpo completo do texto, em formato markdown-like (titulos, bullets, destaques)
    // para renderizacao no app Flutter.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String corpo;

    // Posicao de exibicao dentro da trilha (1-based)
    @Column(nullable = false)
    private Integer ordem;

    // Descricao textual do ponto de insercao de imagem sugerido no design original (opcional)
    @Column(length = 300)
    private String imagemSugerida;
}
