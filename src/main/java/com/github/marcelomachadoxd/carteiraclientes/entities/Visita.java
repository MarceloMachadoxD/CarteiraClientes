package com.github.marcelomachadoxd.carteiraclientes.entities;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tb_visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dataVisita;

    @Column(columnDefinition = "TEXT")
    private String obs;

    private Boolean satisfacao;

    @ManyToOne
    private User responsavel;

    @ManyToOne
    private Cliente cliente;

    public Visita() {
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

    public void setSatisfacao(Boolean satisfação) {
        this.satisfacao = satisfação;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public User getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(User responsavel) {
        this.responsavel = responsavel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visita visita = (Visita) o;
        return Objects.equals(id, visita.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
