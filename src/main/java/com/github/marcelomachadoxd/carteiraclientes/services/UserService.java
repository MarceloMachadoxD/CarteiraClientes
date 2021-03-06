package com.github.marcelomachadoxd.carteiraclientes.services;


import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserInsertDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.repositories.RoleRepository;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
import com.github.marcelomachadoxd.carteiraclientes.services.exceptions.DatabaseException;
import com.github.marcelomachadoxd.carteiraclientes.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPageable(Pageable pageable) {
        Page<User> users = userRepository.findAllPageable(pageable);
        return users.map(x -> new UserDTO(x));
    }

    public UserDTO findById(Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            UserDTO userDTO = new UserDTO(user.get());
            return userDTO;
        } catch (Exception e) {
            throw new ResourceNotFoundException("usuario não encontrado para o id: " + id);
        }
    }

    public void delete(Long id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new DatabaseException("Erro ao deletar Usuario " + id);
        }
    }

    public UserDTO insert(UserInsertDTO userInsertDTO) {
        try {
            Set<Role> roles = new HashSet<>();
            roles.add(new Role(roleRepository.findById(userInsertDTO.getAcessoId()).get()));
            User user = new User();
            user.setNome(userInsertDTO.getNome());
            user.setEmail(userInsertDTO.getEmail());
            user.setPassword(userInsertDTO.getPassword());
            user.setAcesso(roles);
            return new UserDTO(userRepository.save(user));
        } catch (Exception e) {
            throw new DatabaseException("Erro ao inserir Usuario");
        }
    }
}
