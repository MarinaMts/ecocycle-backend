package com.ecocycle.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Figurinha desbloqueada por um usuario. Figurinha repetida via scan nao
 * desbloqueia de novo, apenas reexibe a info (ver documento de decisoes
 * tecnicas, secao 4) - garantido pela unique constraint abaixo.
 */
@Entity
@Table(name = "figurinhas_usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_figurinha_usuario", columnNames = {"user_id", "figurinha_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FigurinhaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "figurinha_id", nullable = false)
    private Figurinha figurinha;

    @Column(name = "desbloqueada_em", nullable = false)
    private LocalDateTime desbloqueadaEm;

    @PrePersist
    protected void aoPersistir() {
        if (this.desbloqueadaEm == null) {
            this.desbloqueadaEm = LocalDateTime.now();
        }
    }
}
