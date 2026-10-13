package com.example.springboot_with_jpa_crud_repo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot_with_jpa_crud_repo.model.User;
import com.example.springboot_with_jpa_crud_repo.service.UserService;

@RestController 
@RequestMapping ("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

      @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers(); 
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user); 
    }



    
}
