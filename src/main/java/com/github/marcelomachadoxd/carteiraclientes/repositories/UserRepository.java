package com.github.marcelomachadoxd.carteiraclientes.repositories;

import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT obj from User obj " +
            "ORDER BY obj.nome ")
    Page<User> findAllPageable(Pageable pageable);
}
