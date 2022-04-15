package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import com.github.marcelomachadoxd.carteiraclientes.repositories.VisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VisitaService {

    @Autowired
    private VisitaRepository visitaRepository;

    public VisitaDTO findById(Long id) {
        Optional<Visita> visita = visitaRepository.findById(id);
        return new VisitaDTO(visita.get());
    }

    public Page<VisitaDTO> findResponsavelById(Long id, Pageable pageable) {
        Page<Visita> visitas = visitaRepository.findByResponsavelId(id, pageable);
        return visitas.map(x -> new VisitaDTO(x));
    }

    public Page<VisitaDTO> findClientelById(Long id, Pageable pageable) {
        Page<Visita> visitas = visitaRepository.findByClienteId(id, pageable);
        return visitas.map(x -> new VisitaDTO(x));
    }

    public Page<VisitaDTO> findbyClienteAndResponsavelId(Long cliId, Long respId, Pageable pageable) {
        Page<Visita> visitas = visitaRepository.findByClienteAndResponsavelId(cliId, respId, pageable);
        return visitas.map(x -> new VisitaDTO(x));
    }

    public void delete(Long id) {
        visitaRepository.deleteById(id);
    }
}
