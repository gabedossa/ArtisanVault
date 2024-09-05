package com.dossa.ArtisanVault.project.dto;

public class LoginRequest {
    private String email;
    private String senha;  // Certifique-se de que o nome é 'senha' e não 'password'

    // Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
