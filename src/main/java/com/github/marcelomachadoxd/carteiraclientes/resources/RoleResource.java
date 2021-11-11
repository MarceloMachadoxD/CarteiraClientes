package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.RoleDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/roles")
public class RoleResource {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleDTO>> findAll() {
        List<RoleDTO> list = roleService.findAll();
        return ResponseEntity.ok().body(list);
    }

    @PostMapping
    public ResponseEntity<RoleDTO> insert(@RequestBody RoleDTO roleDTO){
        roleDTO = roleService.insert(roleDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(roleDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(roleDTO);
    }


}
