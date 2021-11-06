package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public Page<ClienteDTO> findClienteByNome(String nome, Pageable pageable){

        Page<Cliente> cliente = clienteRepository.findByNome(nome, pageable);
        return cliente.map(x -> new ClienteDTO(x));
    }

}
