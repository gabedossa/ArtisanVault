package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import com.dossa.ArtisanVault.project.repository.EmailRegistroRepository;
import com.dossa.ArtisanVault.project.util.EmailNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private static final int MIN_SENHA_LENGTH = 6;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private EmailRegistroRepository emailRegistroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Cliente> findAll(){
        return clienteRepo.findAll();
    }

    public Cliente findById(Long id){
        return clienteRepo.findById(id);
    }

    @Transactional
    public int save(Cliente cliente){
        String email = EmailNormalizer.normalize(cliente.getEmail());
        validateSenha(cliente.getSenha());
        if (!emailRegistroRepository.tryReserve(email)) {
            throw new EmailAlreadyInUseException(email);
        }
        cliente.setEmail(email);
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        return clienteRepo.save(cliente);
    }

    @Transactional
    public int deleteById(Long id){
        Cliente existing = clienteRepo.findById(id);
        int result = clienteRepo.deleteById(id);
        if (result > 0) {
            emailRegistroRepository.release(EmailNormalizer.normalize(existing.getEmail()));
        }
        return result;
    }

    @Transactional
    public int update(Cliente cliente) {
        Cliente existing = clienteRepo.findById(cliente.getIdCliente());
        String email = EmailNormalizer.normalize(cliente.getEmail());
        String existingEmail = EmailNormalizer.normalize(existing.getEmail());
        if (!email.equals(existingEmail)) {
            if (!emailRegistroRepository.tryReserve(email)) {
                throw new EmailAlreadyInUseException(email);
            }
            emailRegistroRepository.release(existingEmail);
        }
        cliente.setEmail(email);

        if (cliente.getSenha() == null || cliente.getSenha().isBlank()) {
            cliente.setSenha(existing.getSenha());
        } else {
            validateSenha(cliente.getSenha());
            cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }
        return clienteRepo.update(cliente);
    }

    public Optional<Cliente> findByEmail(String email) {
        return clienteRepo.findByEmail(EmailNormalizer.normalize(email));
    }

    private void validateSenha(String senha) {
        if (senha == null || senha.length() < MIN_SENHA_LENGTH) {
            throw new WeakPasswordException();
        }
    }
}
