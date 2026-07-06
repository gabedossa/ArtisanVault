package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
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
    private PasswordEncoder passwordEncoder;

    public List<Cliente> findAll(){
        return clienteRepo.findAll();
    }

    public Cliente findById(Long id){
        return clienteRepo.findById(id);
    }

    public int save(Cliente cliente){
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        return clienteRepo.save(cliente);
    }

    public int deleteById(Long id){
        return clienteRepo.deleteById(id);
    }

    public int update(Cliente cliente) {
        if (cliente.getSenha() == null || cliente.getSenha().isBlank()) {
            Cliente existing = clienteRepo.findById(cliente.getIdCliente());
            cliente.setSenha(existing.getSenha());
        } else {
            cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }
        return clienteRepo.update(cliente);
    }

    public Optional<Cliente> findByEmail(String email) {
        return clienteRepo.findByEmail(email);
    }
}
