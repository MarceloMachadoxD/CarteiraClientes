package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.VisitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/visitas")
public class VisitaResource {

    @Autowired
    private VisitaService visitaService;

    @GetMapping("/{id}")
    public ResponseEntity<VisitaDTO> findById(@PathVariable Long id){
        VisitaDTO visitaDTO = visitaService.findById(id);
        return ResponseEntity.ok().body(visitaDTO);
    }


}
