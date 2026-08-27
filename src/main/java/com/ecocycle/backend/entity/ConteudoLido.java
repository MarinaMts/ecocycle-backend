package com.ecocycle.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Marca que um usuario leu um conteudo educativo. A leitura libera o quiz
 * correspondente (ver documento de decisoes tecnicas, secao 2).
 */
@Entity
@Table(name = "conteudos_lidos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conteudo_lido_user_conteudo", columnNames = {"user_id", "conteudo_educativo_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConteudoLido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_educativo_id", nullable = false)
    private ConteudoEducativo conteudoEducativo;

    @Column(name = "lido_em", nullable = false)
    private LocalDateTime lidoEm;

    @PrePersist
    protected void aoPersistir() {
        if (this.lidoEm == null) {
            this.lidoEm = LocalDateTime.now();
        }
    }
}
