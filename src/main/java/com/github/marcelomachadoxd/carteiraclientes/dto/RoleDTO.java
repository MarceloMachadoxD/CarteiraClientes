package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Role;

public class RoleDTO {

    private Long id;
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
