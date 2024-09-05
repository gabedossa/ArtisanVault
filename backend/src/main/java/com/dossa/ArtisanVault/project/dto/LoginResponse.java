package com.dossa.ArtisanVault.project.dto;

public class LoginResponse {
    private String email;
    private String userType;

    public LoginResponse(String email, String userType) {
    }

    public String getEmail() {
        return email;
    }

    public String getUserType() {
        return userType;
    }
}
