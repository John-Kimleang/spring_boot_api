package com.example.springboot_with_jpa_crud_repo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Long>{

    java.util.Optional<User> findByEmail(String email);
    
}
