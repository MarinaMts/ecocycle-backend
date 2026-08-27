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
 * Ponto de coleta (PEV) de lixo eletronico. Lista fixa cadastrada via seed
 * nesta fase - sem integracao com fonte externa como Green Eletron
 * (ver documento de decisoes tecnicas, secao 6).
 * O back-end fornece apenas as coordenadas; quem desenha o mapa e consome
 * a API do Google Maps e o app Flutter.
 */
@Entity
@Table(name = "pontos_coleta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PontoColeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 300)
    private String endereco;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Ex.: "Pilhas", "Baterias", "Celulares", "Computadores"
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ponto_coleta_tipos_residuo", joinColumns = @JoinColumn(name = "ponto_coleta_id"))
    @Column(name = "tipo_residuo", length = 100)
    @Builder.Default
    private List<String> tiposResiduoAceitos = new ArrayList<>();

    // Texto livre, ex.: "Seg a Sex, 9h-18h" (formato simples, sem modelagem de horario complexa)
    @Column(name = "horario_funcionamento", length = 200)
    private String horarioFuncionamento;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
