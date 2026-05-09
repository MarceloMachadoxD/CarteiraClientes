package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

public class UserDTO {

    @Schema(example = "2")
    private Long id;
    @NotBlank
    @Schema(example = "João Corretor")
    private String nome;
    @Email
    private String email;
    @NotBlank
    Set<RoleDTO> acesso = new HashSet<>();

    public UserDTO() {
    }

    public UserDTO(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.nome = user.getNome();
        this.email = user.getEmail();
        user.getAcesso().forEach(role -> this.acesso.add(new RoleDTO(role)));
    }

    public UserDTO(UserInsertDTO userInsertDTO) {
        this.nome = userInsertDTO.getNome();
        this.email = userInsertDTO.getEmail();
        Set<RoleDTO> roles = new HashSet<>();
        roles.add(new RoleDTO(userInsertDTO.getAcessoId(), null));
        this.acesso = roles;
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

    public Set<RoleDTO> getAcesso() {
        return acesso;
    }
}
