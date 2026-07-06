package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.repository.PortifolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortifolioService {
    @Autowired
    private PortifolioRepository portRepo;

    public List<Portifolio> findAll(){return portRepo.findAll();}

    public Portifolio findById(Long id){
        return portRepo.findById(id);
    }

    public Portifolio save(Portifolio portifolio){
        return portRepo.save(portifolio);
    }

    public int deleteById(Long id){
        return portRepo.deleteById(id);
    }

}
