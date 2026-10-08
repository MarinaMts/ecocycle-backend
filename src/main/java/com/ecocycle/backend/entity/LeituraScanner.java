package com.ecocycle.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de cada chamada ao scanner (POST /scanner/reconhecer), para
 * acompanhar como o modelo TFLite se comporta no uso real - nao e usado
 * para nenhuma regra de negocio, apenas para analise posterior (ver
 * mudancas no back-end para o scanner com IA, 07/10/2026, secao opcional).
 */
@Entity
@Table(name = "leitura_scanner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeituraScanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false, length = 100)
    private String identificador;

    @Column(nullable = false)
    private Double confianca;

    @Column(nullable = false)
    private boolean reconhecido;

    // Busca manual do app - chama o mesmo endpoint com confianca = 100
    @Column(nullable = false)
    private boolean manual;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void aoPersistir() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }
}
