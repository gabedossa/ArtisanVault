package com.dossa.ArtisanVault.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LoginResponse {
    private String email;
    private String userType;
    private Long userId;
    private String nome;
    private String token;

    public LoginResponse(String email, String userType, Long userId, String nome, String token) {
        this.email = email;
        this.userType = userType;
        this.userId = userId;
        this.nome = nome;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getUserType() {
        return userType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNome() {
        return nome;
    }

    @JsonIgnore
    public String getToken() {
        return token;
    }
}
