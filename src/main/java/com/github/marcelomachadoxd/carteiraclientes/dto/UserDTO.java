package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDTO {


    private Long id;
    private String nome;
    private String email;
    Set<Role> acesso = new HashSet<>();

    public UserDTO() {
    }


    public UserDTO(Long id, String nome, String email, Set<Role> acesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.acesso = acesso;
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.nome = user.getNome();
        this.email = user.getEmail();
        this.acesso = user.getAcesso();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getAcesso() {
        return acesso;
    }

}
