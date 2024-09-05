package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Servico;
import com.dossa.ArtisanVault.project.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {

    @Autowired
    private ServiceRepository servRepo;

    public List<Servico> findAll(){return servRepo.findAll();}

    public int save(Servico service){
        return servRepo.save(service);
    }

    public int deleteById(Long id){return servRepo.deleteById(id);}
}
