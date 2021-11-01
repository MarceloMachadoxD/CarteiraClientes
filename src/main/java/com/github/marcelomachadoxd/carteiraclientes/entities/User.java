package com.github.marcelomachadoxd.carteiraclientes.entities;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> acesso = new HashSet<>();

    @OneToMany
    private List<Visita> visitas = new ArrayList<>();

    public User() {
    }


    public User(Long id, String nome, String email, String password, Set<Role> acesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.acesso = acesso;
    }


    public User(Long id, String nome, String email, String password, Set<Role> acesso, List<Visita> visitas) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.acesso = acesso;
        this.visitas = visitas;
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

    public void setNome(String name) {
        this.nome = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getAcesso() {
        return acesso;
    }

    public List<Visita> getVisitas() {
        return visitas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
