package com.dossa.ArtisanVault.project.entity;

public class LoginClienteResponse {
    private String tipoUsuario;

    public LoginClienteResponse (String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }
}
