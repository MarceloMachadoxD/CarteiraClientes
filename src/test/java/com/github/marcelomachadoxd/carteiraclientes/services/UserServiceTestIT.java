package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserInsertDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import javax.transaction.Transactional;
import java.util.Optional;

@SpringBootTest
@Transactional
class UserServiceTestIT {
    User user;

    PageRequest pageRequest;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UserInsertDTO userInsertDTO;

    private Role role;

    @BeforeEach
    void Setup() {
        user = new User();
        user.setNome("marcelo");
        user.setId(1L);
        pageRequest = PageRequest.of(0, 10);

        role = new Role();
        role.setId(1L);
        role.setNome("ROLE_ADMIN");

        userInsertDTO = new UserInsertDTO();
        userInsertDTO.setNome("Nome Válido");
        userInsertDTO.setEmail("email@email.com");
        userInsertDTO.setPassword("123456");
        userInsertDTO.setAcessoId(role.getId());

    }

    @Test
    void findById() {
        UserDTO userResultado = userService
            .findById(1L);
        Assertions.assertEquals(userResultado.getNome(), user.getNome());
    }

    @Test
    void insertUser() {
        UserDTO resultado = userService.insert(userInsertDTO);
        Assertions.assertEquals(resultado.getNome(), userInsertDTO.getNome());
    }

    @Test
    void deleteUser() {
        userService.delete(1L);
        Assertions.assertEquals(userRepository.findById(1L), Optional.empty());
    }
}