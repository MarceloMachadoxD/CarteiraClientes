package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserInsertDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.repositories.RoleRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
import com.github.marcelomachadoxd.carteiraclientes.services.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private Long existingId, notExistId;
    private String existingNome, notExistNome;
    private User user;
    private UserDTO userDTO;
    private UserInsertDTO userInsertDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        existingId = 1L;
        notExistId = 2L;
        existingNome = "Nome Válido";
        user.setId(existingId);
        user.setNome(existingNome);

        pageable = PageRequest.of(0, 10);

        userInsertDTO = new UserInsertDTO();
        userInsertDTO.setNome("Nome Válido");
        userInsertDTO.setEmail("email@email.com");
        userInsertDTO.setPassword("123456");
        userInsertDTO.setAcessoId(existingId);

        Mockito.when(userRepository.save(any())).thenReturn(user);
        Mockito.when(userRepository.findById(existingId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(notExistId)).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findById(existingId)).thenReturn(Optional.of(new Role(existingId, "ROLE_ADMIN")));

        Mockito.doNothing().when(userRepository).deleteById(existingId);
        Mockito.doThrow(RuntimeException.class).when(userRepository).deleteById(notExistId);

        Page<User> userPage = new PageImpl<>(List.of(user));
        Mockito.when(userRepository.findAllPageable(any())).thenReturn(userPage);
    }

    @Test
    void findByIdShouldReturnUser() {
        userDTO = userService.findById(existingId);
        assertEquals(userDTO.getNome(), existingNome);
    }

    @Test
    void findByNotExistedIdShouldThrowsException() {
        assertThrows(RuntimeException.class, () -> userService.findById(notExistId));
    }

    @Test
    void saveShouldReturnUser() {
        UserDTO userDTOInsert = new UserDTO();
        userDTOInsert.setNome("Nome Válido");
        UserDTO userDTOresult = userService.insert(userInsertDTO);
        assertEquals(userDTOInsert.getNome(), userDTOresult.getNome());
    }

    @Test
    void deleteShouldCallDeleteById() {
        userService.delete(existingId);
        verify(userRepository, times(1)).deleteById(existingId);
    }

    @Test
    void deleteWithNonExistingIdShouldThrowDatabaseException() {
        assertThrows(DatabaseException.class, () -> userService.delete(notExistId));
    }

    @Test
    void findAllPageableShouldReturnPage() {
        Page<UserDTO> result = userService.findAllPageable(pageable);
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);
    }
}
