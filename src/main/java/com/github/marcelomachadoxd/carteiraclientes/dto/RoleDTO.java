package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public class RoleDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "ROLE_CORRETOR")
    private String nome;

    public RoleDTO() {
    }

    public RoleDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.nome = role.getNome();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
