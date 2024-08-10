package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class ArtistaService {
    @Autowired
    private ArtistaRepository artistaRepo;

    public List<Artista> findAll(){
        return artistaRepo.findAll();
    }

    public Artista findById(Long id){
        return artistaRepo.findById(id);
    }

    public int save(Artista artista){
        return artistaRepo.save(artista);
    }

    public int deleteById(Long id){
        return artistaRepo.deleteById(id);
    }
    public int update(Artista artista) {
        return artistaRepo.update(artista);
    }
}
