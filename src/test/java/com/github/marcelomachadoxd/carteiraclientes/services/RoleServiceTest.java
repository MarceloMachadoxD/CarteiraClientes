package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.RoleDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import com.github.marcelomachadoxd.carteiraclientes.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        Mockito.when(roleRepository.findAll()).thenReturn(List.of(new Role(1L, "ROLE_ADMIN")));
        Mockito.when(roleRepository.save(any())).thenReturn(new Role(1L, "ROLE_TEST"));
    }

    @Test
    void findAllShouldReturnList() {
        List<RoleDTO> result = roleService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void insertShouldCallSaveAndReturnRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setNome("ROLE_TEST");

        RoleDTO result = roleService.insert(roleDTO);

        verify(roleRepository).save(any());
        assertEquals("ROLE_TEST", result.getNome());
    }
}
