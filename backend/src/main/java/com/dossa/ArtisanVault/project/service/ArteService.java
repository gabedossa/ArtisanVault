package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Arte;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.repository.ArteRepository;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ArteService {
    @Autowired
    private ArteRepository arteRepo;

    public List<Arte> findAll(){
        return arteRepo.findAll();
    }

    public Arte findById(Long id){
        return arteRepo.findById(id);
    }

    public int save(Arte arte){
        return arteRepo.save(arte);
    }

    public int deleteById(Long id){
        return arteRepo.deleteById(id);
    }
}
