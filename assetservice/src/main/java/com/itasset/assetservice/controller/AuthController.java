package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.AuthResponse;
import com.itasset.assetservice.dto.LoginRequest;
import com.itasset.assetservice.dto.RegisterRequest;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.Role;
import com.itasset.assetservice.security.JwtUtil;
import com.itasset.assetservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Day 18: the public login/register booth (SecurityConfig lets /api/auth/** through unauthenticated)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // create the account (password hashed in the service), then hand back a token so they're logged in
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setUsername(req.username());
        user.setDepartment(req.department());
        user.setRole(Role.EMPLOYEE); // new signups default to EMPLOYEE; admins are promoted later
        User saved = userService.registerUser(user, req.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenFor(saved));
    }

    // verify username + password against the stored hash; on success issue a token
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = userService.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return tokenFor(user);
    }

    private AuthResponse tokenFor(User user) {
        String role = user.getRole().name();
        return new AuthResponse(jwtUtil.generateToken(user.getUsername(), role), user.getUsername(), role);
    }
}
