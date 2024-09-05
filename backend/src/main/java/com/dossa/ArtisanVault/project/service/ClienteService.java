package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepo;

    public List<Cliente> findAll(){
        return clienteRepo.findAll();
    }

    public Cliente findById(Long id){
        return clienteRepo.findById(id);
    }

    public int save(Cliente cliente){
        return clienteRepo.save(cliente);
    }

    public int deleteById(Long id){
        return clienteRepo.deleteById(id);
    }

    public int update(Cliente cliente) {
        return clienteRepo.update(cliente);
    }

    public Optional<Cliente> verificaArtista(String email, String senha){
        return clienteRepo.LoginCliente(email, senha);
    }
    public Optional<Cliente> LoginArtista(String email, String senha){
        return clienteRepo.LoginCliente(email, senha);
    }
}
