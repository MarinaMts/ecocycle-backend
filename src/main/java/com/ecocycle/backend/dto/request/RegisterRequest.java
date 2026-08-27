package com.ecocycle.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    @NotBlank(message = "O apelido e obrigatorio")
    @Size(min = 3, max = 20, message = "O apelido deve ter entre 3 e 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "O apelido deve conter apenas letras, numeros e underscore")
    private String apelido;

    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
    private String senha;

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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
