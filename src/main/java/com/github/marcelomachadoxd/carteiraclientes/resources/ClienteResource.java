package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/clientes")
public class ClienteResource {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/id/{id}")
    public ResponseEntity<ClienteDTO> findById(@PathVariable Long id) {
        ClienteDTO cliente = clienteService.findById(id);
        return ResponseEntity.ok().body(cliente);
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<Page<ClienteDTO>> findByNome(@PathVariable String nome, Pageable pageable) {
        Page<ClienteDTO> clienteDTO = clienteService.findClienteByNome(nome, pageable);
        return ResponseEntity.ok().body(clienteDTO);
    }

    @GetMapping()  //{{host}}/clientes?margem=5&qtdQuartos=2&qtdBanheiros=1&qtdVagas=1&metragem=45&valorMaximo=150000
    public ResponseEntity<Page<ClienteDTO>> findByInteresses(
        @RequestParam(name = "margem", defaultValue = "0") Integer margem,
        @RequestParam(name = "qtdQuartos", defaultValue = "0") Integer qtdQuartos,
        @RequestParam(name = "qtdBanheiros", defaultValue = "0") Integer qtdBanheiros,
        @RequestParam(name = "qtdVagas", defaultValue = "0") Integer qtdVagas,
        @RequestParam(name = "metragem", defaultValue = "0") Integer metragem,
        @RequestParam(name = "valorMaximo", defaultValue = "0") Integer valorMaximo,
        Pageable pageable) {
        Page<ClienteDTO> clienteDTO = clienteService.findByInteresses(margem, qtdQuartos, qtdBanheiros, qtdVagas, metragem, valorMaximo, pageable);
        return ResponseEntity.ok().body(clienteDTO);
    }

    @PostMapping()
    public ResponseEntity<ClienteDTO> insert(@RequestBody ClienteDTO clienteDTO) {
        clienteDTO = clienteService.insert(clienteDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(clienteDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(clienteDTO);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody ClienteDTO clienteDTO) {
        clienteService.update(id, clienteDTO);
        return ResponseEntity.ok().build();
    }
}
