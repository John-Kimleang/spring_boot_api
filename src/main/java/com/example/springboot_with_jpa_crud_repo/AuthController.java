package com.example.springboot_with_jpa_crud_repo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenService revokedTokenService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RevokedTokenService revokedTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.revokedTokenService = revokedTokenService;
    }

    @PostMapping({ "/signin", "/login" })
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElse(null);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
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

    public record SignInRequest(String email, String password) {
    }

    public record AuthResponse(String token, String tokenType, long expiresIn) {
    }
}
