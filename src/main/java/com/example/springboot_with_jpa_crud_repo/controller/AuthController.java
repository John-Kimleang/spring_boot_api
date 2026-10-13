package com.example.springboot_with_jpa_crud_repo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import com.example.springboot_with_jpa_crud_repo.dto.AuthResponse;
import com.example.springboot_with_jpa_crud_repo.dto.RegisterRequest;
import com.example.springboot_with_jpa_crud_repo.dto.SignInRequest;
import com.example.springboot_with_jpa_crud_repo.model.User;
import com.example.springboot_with_jpa_crud_repo.repository.UserRepository;
import com.example.springboot_with_jpa_crud_repo.service.JwtService;
import com.example.springboot_with_jpa_crud_repo.service.RevokedTokenService;
import com.example.springboot_with_jpa_crud_repo.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenService revokedTokenService;
    private final UserService userService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RevokedTokenService revokedTokenService, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.revokedTokenService = revokedTokenService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.name() == null || request.name().isBlank()
                || request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body("Name, email, and password are required");
        }
        String email = request.email().trim().toLowerCase();
        if (email.length() > 191) {
            return ResponseEntity.badRequest().body("Email must be 191 characters or fewer");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(request.password());
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping({ "/signin", "/login" })
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        if (request.email() == null || request.password() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElse(null);
        if (user == null) {
            logger.warn("Login failed: no user found for normalized email {}", request.email().trim().toLowerCase());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            logger.warn("Login failed: user {} has no password hash", user.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        logger.info("Login password check for {}: hashLength={}, matches={}",
                user.getEmail(), user.getPasswordHash().length(), passwordMatches);
        if (!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        return ResponseEntity.ok(new AuthResponse(jwtService.createToken(user.getEmail()), "Bearer",
                jwtService.getTokenLifetimeSeconds()));
    }

    @PostMapping({ "/signout", "/logout" })
    public ResponseEntity<String> signOut(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("A Bearer token is required");
        }
        String token = authorization.substring(7);
        try {
            Claims claims = jwtService.parseToken(token);
            revokedTokenService.revoke(token, claims.getExpiration().toInstant());
            return ResponseEntity.ok("Signed out successfully");
        } catch (JwtException | IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

}
