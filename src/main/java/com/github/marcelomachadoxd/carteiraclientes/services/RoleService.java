package com.github.marcelomachadoxd.carteiraclientes.services;

import com.github.marcelomachadoxd.carteiraclientes.dto.RoleDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.Role;
import com.github.marcelomachadoxd.carteiraclientes.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleDTO> findAll(){
        List<Role> list = roleRepository.findAll();
        return list.parallelStream().map(RoleDTO::new).collect(Collectors.toList());
    }

    public RoleDTO insert(RoleDTO roleDTO){
        Role role = new Role();
        role.setNome(roleDTO.getNome());
        roleRepository.save(role);
        return new RoleDTO(role);
    }

}
