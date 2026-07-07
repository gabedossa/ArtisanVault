package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import com.dossa.ArtisanVault.project.util.EmailNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Cliente> findAll(){
        return clienteRepo.findAll();
    }

    public Cliente findById(Long id){
        return clienteRepo.findById(id);
    }

    public int save(Cliente cliente){
        String email = EmailNormalizer.normalize(cliente.getEmail());
        if (isEmailTaken(email)) {
            throw new EmailAlreadyInUseException(email);
        }
        cliente.setEmail(email);
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        return clienteRepo.save(cliente);
    }

    public int deleteById(Long id){
        return clienteRepo.deleteById(id);
    }

    public int update(Cliente cliente) {
        Cliente existing = clienteRepo.findById(cliente.getIdCliente());
        String email = EmailNormalizer.normalize(cliente.getEmail());
        if (!email.equals(EmailNormalizer.normalize(existing.getEmail())) && isEmailTaken(email)) {
            throw new EmailAlreadyInUseException(email);
        }
        cliente.setEmail(email);

        if (cliente.getSenha() == null || cliente.getSenha().isBlank()) {
            cliente.setSenha(existing.getSenha());
        } else {
            cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }
        return clienteRepo.update(cliente);
    }

    public Optional<Cliente> findByEmail(String email) {
        return clienteRepo.findByEmail(EmailNormalizer.normalize(email));
    }

    // O mesmo e-mail nao pode existir em cliente nem em artista (login resolve por
    // e-mail global, procurando cliente antes de artista).
    private boolean isEmailTaken(String normalizedEmail) {
        return clienteRepo.findByEmail(normalizedEmail).isPresent()
                || artistaRepository.findByEmail(normalizedEmail).isPresent();
    }
}
