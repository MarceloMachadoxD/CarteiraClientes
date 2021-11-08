package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.Visita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Long> {

    @Query("SELECT obj FROM Visita obj WHERE " +
            "obj.responsavel.id = :id")
    Page<Visita> findByResponsavelId(Long id, Pageable pageable);

    @Query("SELECT obj FROM Visita obj WHERE " +
            "obj.cliente.id = :id")
    Page<Visita> findByClienteId(Long id, Pageable pageable);

    @Query("SELECT obj FROM Visita obj WHERE " +
            "obj.cliente.id = :cliId " +
            "AND " +
            "obj.responsavel.id = :respId")
    Page<Visita> findByClienteAndResponsavelId(Long cliId, Long respId, Pageable pageable);

}
