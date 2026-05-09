package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;

import io.swagger.v3.oas.annotations.media.Schema;

public class ClienteDadosBasicosDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Maria Silva")
    private String nome;

    public ClienteDadosBasicosDTO() {
    }

    public ClienteDadosBasicosDTO(Long id, String nome, Integer qtdQuartos, Integer qtdBanheiros, Integer qtdVagas, Integer metragem, Integer valorMaximo, String obs) {
        this.id = id;
        this.nome = nome;
    }

    public ClienteDadosBasicosDTO(Cliente cliente) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
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
