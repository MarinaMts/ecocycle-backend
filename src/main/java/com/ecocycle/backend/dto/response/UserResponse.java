package com.ecocycle.backend.dto.response;

import com.ecocycle.backend.entity.User;

import java.time.LocalDateTime;

/**
 * DTO de resposta do usuario. Nunca inclui campos sensiveis (ex.: senha/hash).
 */
public class UserResponse {

    private Long id;
    private String email;
    private String apelido;
    private String avatar;
    private LocalDateTime criadoEm;
    private LocalDateTime termosAceitosEm;
    private String termosVersao;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String apelido, String avatar, LocalDateTime criadoEm) {
        this.id = id;
        this.email = email;
        this.apelido = apelido;
        this.avatar = avatar;
        this.criadoEm = criadoEm;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getApelido(),
                user.getAvatar() != null ? user.getAvatar().name() : null,
                user.getCriadoEm()
        ).comTermos(user.getTermosAceitosEm(), user.getTermosVersao());
    }

    private UserResponse comTermos(LocalDateTime aceitosEm, String versao) {
        this.termosAceitosEm = aceitosEm;
        this.termosVersao = versao;
        return this;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getTermosAceitosEm() {
        return termosAceitosEm;
    }

    public void setTermosAceitosEm(LocalDateTime termosAceitosEm) {
        this.termosAceitosEm = termosAceitosEm;
    }

    public String getTermosVersao() {
        return termosVersao;
    }

    public void setTermosVersao(String termosVersao) {
        this.termosVersao = termosVersao;
    }
}
