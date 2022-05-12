package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import com.github.marcelomachadoxd.carteiraclientes.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public Page<ClienteDTO> findClienteByNome(String nome, Pageable pageable) {

        Page<Cliente> cliente = clienteRepository.findByNome(nome, pageable);
        return cliente.map(x -> new ClienteDTO(x));
    }

    public Page<ClienteDTO> findByInteresses(Integer margem, Integer qtdQuartos, Integer qtdBanheiros, Integer qtdVagas
        , Integer metragem, Integer valorMaximo, Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findByInteresses(
            margem,
            qtdQuartos,
            qtdBanheiros,
            qtdVagas,
            metragem,
            valorMaximo,
            pageable
        );
        return clientes.map(x -> new ClienteDTO(x));
    }

    public ClienteDTO findById(Long id) {
        try {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return new ClienteDTO(cliente.get());
        } catch (Exception e) {
            throw new RuntimeException("Cliente não encontrado");
        }
    }

    public ClienteDTO insert(ClienteDTO clienteDTO) {

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDTO.getNome());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setMetragem(clienteDTO.getMetragem());
        cliente.setObs(clienteDTO.getObs());
        cliente.setQtdBanheiros(clienteDTO.getQtdBanheiros());
        cliente.setValorMaximo(clienteDTO.getValorMaximo());
        cliente.setQtdQuartos(clienteDTO.getQtdQuartos());
        cliente.setQtdVagas(clienteDTO.getQtdVagas());
        return new ClienteDTO(clienteRepository.save(cliente));
    }

    public void delete(Long id) {
        clienteRepository.deleteById(id);
    }

    public void update(Long id, ClienteDTO clienteDTO) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        cliente.get().setNome(clienteDTO.getNome());
        cliente.get().setEmail(clienteDTO.getEmail());
        cliente.get().setMetragem(clienteDTO.getMetragem());
        cliente.get().setObs(clienteDTO.getObs());
        cliente.get().setQtdBanheiros(clienteDTO.getQtdBanheiros());
        cliente.get().setValorMaximo(clienteDTO.getValorMaximo());
        cliente.get().setQtdQuartos(clienteDTO.getQtdQuartos());
        cliente.get().setQtdVagas(clienteDTO.getQtdVagas());
        clienteRepository.save(cliente.get());
    }
}
