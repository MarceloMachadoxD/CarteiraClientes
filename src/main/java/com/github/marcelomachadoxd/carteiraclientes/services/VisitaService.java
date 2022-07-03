package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.VisitaRepository;
import com.github.marcelomachadoxd.carteiraclientes.services.exceptions.DatabaseException;
import com.github.marcelomachadoxd.carteiraclientes.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        try {
            Optional<Visita> visita = visitaRepository.findById(id);
            return new VisitaDTO(visita.get());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Visita não encontrada para o id: " + id);
        }
    }

    public Page<VisitaDTO> findResponsavelById(Long id, Pageable pageable) {
        try {
            Optional<Visita> visita = visitaRepository.findById(id);
            Page<Visita> visitas = visitaRepository.findByResponsavelId(id, pageable);
            return visitas.map(x -> new VisitaDTO(x));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Visita não encontrada Responsavel id: " + id);
        }
    }

    public Page<VisitaDTO> findClientelById(Long id, Pageable pageable) {
        try {
            Page<Visita> visitas = visitaRepository.findByClienteId(id, pageable);
            return visitas.map(x -> new VisitaDTO(x));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Visita não encontrada Cliente id: " + id);
        }
    }

    public Page<VisitaDTO> findbyClienteAndResponsavelId(Long cliId, Long respId, Pageable pageable) {
        try {
            Page<Visita> visitas = visitaRepository.findByClienteAndResponsavelId(cliId, respId, pageable);
            return visitas.map(x -> new VisitaDTO(x));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Visita não encontrada Cliente id: " + cliId + " e Responsavel id: " + respId);
        }
    }

    public void delete(Long id) {
        visitaRepository.deleteById(id);
    }

    public VisitaDTO insert(VisitaDTO visitaDTO) {
        Visita visita = new Visita(visitaDTO);

        try {
            User user = userRepository.findById(visitaDTO.getResponsavel().getId()).get();
            visita.setResponsavel(user);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Responsável não encontrado para o id: " + visitaDTO.getResponsavel().getId());
        }


        try {
            Cliente cliente = clienteRepository.findById(visitaDTO.getCliente().getId()).get();
            visita.setCliente(cliente);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Cliente não encontrado para o id: " + visitaDTO.getCliente().getId());
        }

        return new VisitaDTO(visitaRepository.save(visita));
    }
}
