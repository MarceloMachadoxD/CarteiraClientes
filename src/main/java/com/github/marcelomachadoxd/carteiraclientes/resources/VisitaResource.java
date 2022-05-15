package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.VisitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/visitas")
public class VisitaResource {

    @Autowired
    private VisitaService visitaService;

    @GetMapping("/{id}")
    public ResponseEntity<VisitaDTO> findById(@PathVariable Long id) {
        VisitaDTO visitaDTO = visitaService.findById(id);
        return ResponseEntity.ok().body(visitaDTO);
    }

    @GetMapping("/responsavel/{id}")
    public ResponseEntity<Page<VisitaDTO>> findByResponsavelId(@PathVariable Long id, Pageable pageable) {
        Page<VisitaDTO> visitaDTO = visitaService.findResponsavelById(id, pageable);
        return ResponseEntity.ok().body(visitaDTO);
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<Page<VisitaDTO>> findByClienteId(@PathVariable Long id, Pageable pageable) {
        Page<VisitaDTO> visitaDTO = visitaService.findClientelById(id, pageable);
        return ResponseEntity.ok().body(visitaDTO);
    }

    @GetMapping()
    public ResponseEntity<Page<VisitaDTO>> findByClienteId(
            @RequestParam(name = "cliId", defaultValue = "1") Long cliId,
            @RequestParam(name = "respId", defaultValue = "1") Long respId,
            Pageable pageable) {
        Page<VisitaDTO> visitaDTO = visitaService.findbyClienteAndResponsavelId(cliId, respId, pageable);
        return ResponseEntity.ok().body(visitaDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        visitaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<VisitaDTO> insert(@Valid @RequestBody VisitaDTO visitaDTO) {
        VisitaDTO visitaDTO2 = visitaService.insert(visitaDTO);
        return ResponseEntity.ok().body(visitaDTO2);
    }

}
