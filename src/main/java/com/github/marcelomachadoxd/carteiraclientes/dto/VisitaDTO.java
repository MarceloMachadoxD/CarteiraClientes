package com.github.marcelomachadoxd.carteiraclientes.dto;

import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;

import java.time.Instant;

public class VisitaDTO {

    private Long id;
    private Instant dataVisita;
    private String obs;
    private Boolean satisfacao;
    private UserDTO responsavel = new UserDTO();
    private ClienteDadosBasicosDTO cliente = new ClienteDadosBasicosDTO();

    public VisitaDTO() {
    }

    public VisitaDTO(Long id, Instant dataVisita, String obs, Boolean satisfacao) {
        this.id = id;
        this.dataVisita = dataVisita;
        this.obs = obs;
        this.satisfacao = satisfacao;

    }

    public VisitaDTO(Visita visita) {
        this.id = visita.getId();
        this.dataVisita = visita.getDataVisita();
        this.obs = visita.getObs();
        this.satisfacao = visita.getSatisfacao();

        this.responsavel.setId(visita.getResponsavel().getId());
        this.responsavel.setNome(visita.getResponsavel().getNome());
        this.responsavel.setEmail(visita.getResponsavel().getEmail());

        this.cliente.setNome(visita.getCliente().getNome());
        this.cliente.setId(visita.getCliente().getId());
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

    public UserDTO getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(UserDTO responsavel) {
        this.responsavel = responsavel;
    }

    public ClienteDadosBasicosDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDadosBasicosDTO cliente) {
        this.cliente = cliente;
    }
}
