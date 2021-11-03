package com.github.marcelomachadoxd.carteiraclientes.resources;

import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users")
public class UserResource {

    @Autowired
    private UserService userService;


    @GetMapping()
    public ResponseEntity<Page<UserDTO>> findAllPageable(Pageable pageable){
        Page<UserDTO> users = userService.findAllPageable(pageable);

        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        UserDTO user = userService.findById(id);

        return ResponseEntity.ok().body(user);


    }
}