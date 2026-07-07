package com.dossa.ArtisanVault.project.service;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("O e-mail " + email + " ja esta em uso.");
    }
}
