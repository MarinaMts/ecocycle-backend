package com.ecocycle.backend.entity;

import com.ecocycle.backend.enums.TipoFigurinha;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Figurinha do album de colecao (mecanica de motivacao).
 * Entidade unica cobrindo os dois tipos (ver documento de decisoes tecnicas, secao 4):
 * - QUIZ: desbloqueada ao concluir o quiz correspondente pela primeira vez
 * - SCAN: desbloqueada via scanner TFLite (implementado na Semana 3)
 */
@Entity
@Table(name = "figurinhas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_figurinha_codigo", columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Figurinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo estavel, ex.: "FIG-01"
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoFigurinha tipo;

    // Preenchido apenas para figurinhas do tipo QUIZ - vincula ao conteudo/quiz de origem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_educativo_id")
    private ConteudoEducativo conteudoEducativo;

    // Preenchido apenas para figurinhas do tipo SCAN - identificador do componente
    // eletronico reconhecido pelo TFLite (definido na Semana 3)
    @Column(name = "identificador_scan", length = 100)
    private String identificadorScan;

    @Column(nullable = false, length = 500)
    private String descricao;

    // Posicao de exibicao no album
    @Column(nullable = false)
    private Integer ordem;
}
