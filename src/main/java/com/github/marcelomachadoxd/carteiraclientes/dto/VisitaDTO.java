package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;

import java.time.Instant;

public class VisitaDTO {

    private Long id;
    private Instant dataVisita;
    private String obs;
    private Boolean satisfacao;
    private Long responsavelId;
    private String responsavel;
    private Long clienteId;
    private String cliente;

    public VisitaDTO() {
    }

    public VisitaDTO(Long id, Instant dataVisita, String obs, Boolean satisfacao, Long responsavelId, String responsavel, Long clienteId, String cliente) {
        this.id = id;
        this.dataVisita = dataVisita;
        this.obs = obs;
        this.satisfacao = satisfacao;
        this.responsavelId = responsavelId;
        this.responsavel = responsavel;
        this.clienteId = clienteId;
        this.cliente = cliente;
    }

    public VisitaDTO(Visita visita) {
        this.id = visita.getId();
        this.dataVisita = visita.getDataVisita();
        this.obs = visita.getObs();
        this.satisfacao = visita.getSatisfacao();
        this.responsavelId = visita.getResponsavel().getId();
        this.responsavel = visita.getResponsavel().getNome();
        this.clienteId = visita.getCliente().getId();
        this.cliente = visita.getCliente().getNome();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDataVisita() {
        return dataVisita;
    }

    public void setDataVisita(Instant dataVisita) {
        this.dataVisita = dataVisita;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public Boolean getSatisfacao() {
        return satisfacao;
    }

    public void setSatisfacao(Boolean satisfacao) {
        this.satisfacao = satisfacao;
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }


}
