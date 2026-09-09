package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.AuthResponse;
import com.itasset.assetservice.dto.LoginRequest;
import com.itasset.assetservice.dto.RegisterRequest;
import com.itasset.assetservice.entity.RefreshToken;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.Role;
import com.itasset.assetservice.security.JwtUtil;
import com.itasset.assetservice.service.RefreshTokenService;
import com.itasset.assetservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

// Day 18: the public login/register booth (SecurityConfig lets /api/auth/** through unauthenticated)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    // create the account (password hashed in the service), then log them in
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setUsername(req.username());
        user.setDepartment(req.department());
        user.setRole(Role.EMPLOYEE); // new signups default to EMPLOYEE; admins are promoted later
        User saved = userService.registerUser(user, req.password());
        RefreshToken refresh = refreshTokenService.create(saved.getUsername());
        return authResponse(saved, refresh, HttpStatus.CREATED);
    }

    // verify username + password against the stored hash; on success issue access + refresh
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        RefreshToken refresh = refreshTokenService.create(user.getUsername());
        return authResponse(user, refresh, HttpStatus.OK);
    }

    // access token expired? send the refresh cookie -> get a new access token + a rotated refresh cookie
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }
        RefreshToken rotated = refreshTokenService.rotate(refreshToken);
        User user = userService.findByUsername(rotated.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
        return authResponse(user, rotated, HttpStatus.OK);
    }

    // logout -> revoke the refresh token server-side AND clear the cookie in the browser
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        refreshTokenService.revoke(refreshToken);
        ResponseCookie cleared = cookie("", 0); // maxAge 0 tells the browser to delete it
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cleared.toString()).build();
    }

    // access token in the body, refresh token in an httpOnly cookie
    private ResponseEntity<AuthResponse> authResponse(User user, RefreshToken refresh, HttpStatus status) {
        String role = user.getRole().name();
        String access = jwtUtil.generateToken(user.getUsername(), role);
        long maxAge = Duration.between(Instant.now(), refresh.getExpiresAt()).getSeconds();
        ResponseCookie cookie = cookie(refresh.getToken(), maxAge);
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(access, user.getUsername(), role));
    }

    private ResponseCookie cookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)            // JS can't read it -> safe from XSS token theft
                .secure(false)             // ponytail: set true behind HTTPS in prod
                .path("/api/auth")         // only sent to the auth endpoints that need it
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")           // ponytail: basic CSRF mitigation; Strict if refresh is same-site only
                .build();
    }
}
