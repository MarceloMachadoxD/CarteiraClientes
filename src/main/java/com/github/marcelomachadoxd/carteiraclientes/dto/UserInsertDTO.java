package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserInsertDTO {

    private Long id;
    @NotBlank(message = "Campo Obrigatório")
    @Schema(example = "João Corretor")
    private String nome;
    @Email(message = "Email inválido")
    @Schema(example = "joao.corretor@imobiliaria.com.br")
    private String email;
    @Schema(example = "1")
    private Long acessoId;
    @NotBlank(message = "Campo Obrigatório")
    @Schema(example = "senha123")
    private String password;

    public UserInsertDTO() {
    }


    public UserInsertDTO(String nome, String email, Long acessoId, String password) {
        this.nome = nome;
        this.email = email;
        this.password = password;
    }

    public UserInsertDTO(User user) {
        this.id = user.getId();
        this.nome = user.getNome();
        this.email = user.getEmail();
        this.password = user.getPassword();
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

    public Long getAcessoId() {
        return acessoId;
    }

    public void setAcessoId(Long acessoId) {
        this.acessoId = acessoId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
