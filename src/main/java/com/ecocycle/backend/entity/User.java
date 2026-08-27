package com.ecocycle.backend.entity;

import com.ecocycle.backend.enums.Avatar;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_apelido", columnNames = "apelido")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Apelido: 3-20 caracteres, unico, publico, fixo apos criacao (nao editavel)
    @Column(nullable = false, unique = true, length = 20)
    private String apelido;

    // Armazenada com hash (BCrypt) - nunca em texto puro
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Avatar avatar = Avatar.AVATAR_01;

    // Soft delete - preserva historico, progresso e integridade referencial
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    // XP acumulado (mecanica de motivacao - ver documento de decisoes tecnicas, secao 3)
    @Column(name = "xp_total", nullable = false)
    @Builder.Default
    private Integer xpTotal = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoPersistir() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
        if (this.ativo == null) {
            this.ativo = true;
        }
        if (this.avatar == null) {
            this.avatar = Avatar.AVATAR_01;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
