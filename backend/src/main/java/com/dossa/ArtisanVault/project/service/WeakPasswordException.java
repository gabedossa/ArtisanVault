package com.dossa.ArtisanVault.project.service;

public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException() {
        super("A senha deve ter pelo menos 6 caracteres.");
    }
}
