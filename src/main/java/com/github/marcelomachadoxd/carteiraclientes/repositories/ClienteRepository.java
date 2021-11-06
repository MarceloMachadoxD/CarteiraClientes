package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT obj FROM Cliente obj WHERE LOWER(obj.nome) like (  CONCAT(LOWER(:nome), '%' ) ) ")
    Page<Cliente> findByNome(String nome, Pageable pageable);
}
