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

    @Query("SELECT obj FROM Cliente obj WHERE " +
            "( (:valorMaximo = 0 )  OR (:valorMaximo <= (obj.valorMaximo + ( obj.valorMaximo * (:margem/100)) ) ) ) " + // OR (:valorMaximo = obj.valorMaximo )
            "AND " +
            "((:metragem = 0) OR  ((:metragem >= obj.metragem )) OR (:metragem >= (obj.metragem - (obj.metragem * (:margem/100) ))))  " +
            "AND " +
            "((:qtdQuartos = 0) OR (obj.qtdQuartos >= :qtdQuartos  ))  " +
            "AND " +
            "((:qtdBanheiros = 0) OR (obj.qtdBanheiros >= :qtdBanheiros  ))  " +
            "AND " +
            "((:qtdVagas = 0) OR (obj.qtdVagas >= :qtdVagas  )) ")
    Page<Cliente> findByInteresses(Integer margem, Integer qtdQuartos, Integer qtdBanheiros, Integer qtdVagas, Integer metragem, Integer valorMaximo, Pageable pageable);
}
