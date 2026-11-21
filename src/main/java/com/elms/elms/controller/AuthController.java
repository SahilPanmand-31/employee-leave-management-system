package com.elms.elms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elms.elms.dto.AuthResponse;
import com.elms.elms.dto.LoginRequest;
import com.elms.elms.dto.RegisterRequest;
import com.elms.elms.entity.AppUser;
import com.elms.elms.entity.Role;
import com.elms.elms.exception.DuplicateResourceException;
import com.elms.elms.repository.AppUserRepository;
import com.elms.elms.security.JwtUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

// Public endpoints to create an account and get a JWT token
@RestController
@RequestMapping("/auth")
@SecurityRequirements // no token needed for these endpoints in Swagger
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register a new user (role EMPLOYEE by default)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (appUserRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        Role role = (request.role() == null) ? Role.EMPLOYEE : request.role();
        AppUser user = new AppUser(request.username(), passwordEncoder.encode(request.password()), role);
        appUserRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), role.name());
        return new ResponseEntity<>(new AuthResponse(token, user.getUsername(), role), HttpStatus.CREATED);
    }

    // Login and receive a JWT token
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }
}
