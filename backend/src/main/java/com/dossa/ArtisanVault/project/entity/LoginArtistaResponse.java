package com.dossa.ArtisanVault.project.entity;

public class LoginArtistaResponse {
    private String tipoUsuario;

    public LoginArtistaResponse (String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }
}
