package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClienteRepository clienteRepository;

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

    public VisitaDTO insert(VisitaDTO visitaDTO) {
        Visita visita = new Visita(visitaDTO);
        
        User user = userRepository.findById(visitaDTO.getResponsavel().getId()).get();
        visita.setResponsavel(user);

        Cliente cliente = clienteRepository.findById(visitaDTO.getCliente().getId()).get();
        visita.setCliente(cliente);

        return new VisitaDTO(visitaRepository.save(visita));
    }
}
