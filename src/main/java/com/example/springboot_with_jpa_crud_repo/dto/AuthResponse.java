package com.example.springboot_with_jpa_crud_repo.dto;

public record AuthResponse(String token, String tokenType, long expiresIn) {
}
