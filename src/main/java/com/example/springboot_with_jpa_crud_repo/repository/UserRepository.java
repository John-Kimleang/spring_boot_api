package com.example.springboot_with_jpa_crud_repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.springboot_with_jpa_crud_repo.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

    java.util.Optional<User> findByEmail(String email);
    
}
