package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;

public class ClienteDTO {

    private Long id;
    private String nome;
    private String email;
    private Integer qtdQuartos;
    private Integer qtdBanheiros;
    private Integer qtdVagas;
    private Integer metragem;
    private Integer valorMaximo;

    public ClienteDTO() {
    }

    public ClienteDTO(Long id, String nome, String email, Integer qtdQuartos, Integer qtdBanheiros, Integer qtdVagas, Integer metragem, Integer valorMaximo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.qtdQuartos = qtdQuartos;
        this.qtdBanheiros = qtdBanheiros;
        this.qtdVagas = qtdVagas;
        this.metragem = metragem;
        this.valorMaximo = valorMaximo;
    }

    public ClienteDTO(Cliente cliente) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
        this.email = cliente.getEmail();
        this.qtdQuartos = cliente.getQtdQuartos();
        this.qtdBanheiros = cliente.getQtdBanheiros();
        this.qtdVagas = cliente.getQtdVagas();
        this.metragem = cliente.getMetragem();
        this.valorMaximo = cliente.getValorMaximo();
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

    public Integer getQtdQuartos() {
        return qtdQuartos;
    }

    public void setQtdQuartos(Integer qtdQuartos) {
        this.qtdQuartos = qtdQuartos;
    }

    public Integer getQtdBanheiros() {
        return qtdBanheiros;
    }

    public void setQtdBanheiros(Integer qtdBanheiros) {
        this.qtdBanheiros = qtdBanheiros;
    }

    public Integer getQtdVagas() {
        return qtdVagas;
    }

    public void setQtdVagas(Integer qtdVagas) {
        this.qtdVagas = qtdVagas;
    }

    public Integer getMetragem() {
        return metragem;
    }

    public void setMetragem(Integer metragem) {
        this.metragem = metragem;
    }

    public Integer getValorMaximo() {
        return valorMaximo;
    }

    public void setValorMaximo(Integer valorMaximo) {
        this.valorMaximo = valorMaximo;
    }
}
