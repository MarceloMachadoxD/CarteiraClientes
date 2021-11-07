package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import com.github.marcelomachadoxd.carteiraclientes.repositories.VisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VisitaService {

    @Autowired
    private VisitaRepository visitaRepository;

    public VisitaDTO findById(Long id){
        Optional<Visita> visita = visitaRepository.findById(id);
        return new VisitaDTO(visita.get());
    }

}
