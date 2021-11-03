package com.github.marcelomachadoxd.carteiraclientes.services;


import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.entities.User;
import com.github.marcelomachadoxd.carteiraclientes.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPageable(Pageable pageable){
        Page<User> users = userRepository.findAllPageable(pageable);

        return users.map(x -> new UserDTO(x) );

    }


}
