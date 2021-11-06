package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/clientes")
public class ClienteResource {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/{nome}")
    public ResponseEntity<Page<ClienteDTO>> findByNome(@PathVariable String nome, Pageable pageable){
        Page<ClienteDTO> clienteDTO = clienteService.findClienteByNome(nome, pageable);

        return ResponseEntity.ok().body(clienteDTO);
    }


}
