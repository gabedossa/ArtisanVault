package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String senha) {
        System.out.println("Tentando fazer login com email: " + email);

        Optional<Cliente> cliente = clienteRepository.findByEmail(email);
        if (cliente.isPresent()) {
            System.out.println("Cliente encontrado: " + cliente.get().getEmail());
            if (cliente.get().getSenha().equals(senha)) {
                return "Cliente";
            } else {
                System.out.println("Senha incorreta para o cliente: " + email);
                throw new IllegalArgumentException("Credenciais inválidas");
            }
        } else {
            System.out.println("Cliente não encontrado: " + email);
        }

        Optional<Artista> artista = artistaRepository.findByEmail(email);
        if (artista.isPresent()) {
            System.out.println("Artista encontrado: " + artista.get().getEmail());
            if (artista.get().getSenha().equals(senha)) {
                return "Artista";
            } else {
                System.out.println("Senha incorreta para o artista: " + email);
                throw new IllegalArgumentException("Credenciais inválidas");
            }
        } else {
            System.out.println("Artista não encontrado: " + email);
        }

        System.out.println("Credenciais inválidas para: " + email);
        throw new IllegalArgumentException("Credenciais inválidas");}
}
