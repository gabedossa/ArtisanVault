package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.dto.LoginResponse;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import com.dossa.ArtisanVault.project.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(String email, String senha) {
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            if (!passwordEncoder.matches(senha, cliente.getSenha())) {
                throw new BadCredentialsException("Credenciais inválidas");
            }
            String token = jwtService.generateToken(cliente.getEmail(), "CLIENTE", cliente.getIdCliente());
            return new LoginResponse(cliente.getEmail(), "CLIENTE", cliente.getIdCliente(), cliente.getNome(), token);
        }

        Optional<Artista> artistaOpt = artistaRepository.findByEmail(email);
        if (artistaOpt.isPresent()) {
            Artista artista = artistaOpt.get();
            if (!passwordEncoder.matches(senha, artista.getSenha())) {
                throw new BadCredentialsException("Credenciais inválidas");
            }
            String token = jwtService.generateToken(artista.getEmail(), "ARTISTA", artista.getIdArtista());
            return new LoginResponse(artista.getEmail(), "ARTISTA", artista.getIdArtista(), artista.getNome(), token);
        }

        throw new UsernameNotFoundException("Credenciais inválidas");
    }
}
