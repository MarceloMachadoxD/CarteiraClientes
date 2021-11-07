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
            "((obj.valorMaximo >= :valorMaximo  ) OR (obj.valorMaximo <= :valorMaximo  ))   " +
            "AND " +
            "((obj.qtdQuartos >= :qtdQuartos  ) OR (obj.qtdQuartos <= :qtdQuartos  ) )  "+
            "AND " +
            "((obj.qtdBanheiros >= :qtdBanheiros  ) OR (obj.qtdBanheiros <= :qtdBanheiros  ) )  " +
            "AND " +
            "((obj.metragem >= :metragem  ) OR (obj.metragem <= :metragem  ) )  " +
            "AND " +
            "((obj.valorMaximo >= :valorMaximo  ) OR (obj.valorMaximo <= :valorMaximo  ) )  " +
            "AND " +
            "((obj.qtdVagas >= :qtdVagas  ) OR (obj.qtdVagas <= :qtdVagas  ) )  " +
            "")

    Page<Cliente> findByInteresses( Integer qtdQuartos, Integer qtdBanheiros, Integer qtdVagas, Integer metragem, Integer valorMaximo, Pageable pageable);
}
