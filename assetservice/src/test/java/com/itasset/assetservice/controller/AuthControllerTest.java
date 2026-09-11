package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.AuthResponse;
import com.itasset.assetservice.dto.LoginRequest;
import com.itasset.assetservice.entity.RefreshToken;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.Role;
import com.itasset.assetservice.security.JwtUtil;
import com.itasset.assetservice.service.RefreshTokenService;
import com.itasset.assetservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// Day 21 checkpoint: login returns 200 + JWT on valid creds, 401 on wrong password or unknown user.
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks private AuthController controller;

    private User employee() {
        User u = new User();
        u.setUsername("bharat");
        u.setPasswordHash("stored-hash");
        u.setRole(Role.EMPLOYEE);
        return u;
    }

    @Test
    void login_validCredentials_returns200WithToken() {
        User user = employee();
        RefreshToken refresh = new RefreshToken();
        refresh.setToken("refresh-uuid");
        refresh.setExpiresAt(Instant.now().plusSeconds(604800));

        when(userService.findByUsername("bharat")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-pw", "stored-hash")).thenReturn(true);
        when(refreshTokenService.create("bharat")).thenReturn(refresh);
        when(jwtUtil.generateToken("bharat", "EMPLOYEE")).thenReturn("access-jwt");

        ResponseEntity<AuthResponse> response = controller.login(new LoginRequest("bharat", "correct-pw"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access-jwt", response.getBody().accessToken());
        assertEquals("bharat", response.getBody().username());
        assertEquals("EMPLOYEE", response.getBody().role());
    }

    @Test
    void login_wrongPassword_throws401() {
        when(userService.findByUsername("bharat")).thenReturn(Optional.of(employee()));
        when(passwordEncoder.matches("wrong-pw", "stored-hash")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.login(new LoginRequest("bharat", "wrong-pw")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_unknownUser_throws401() {
        when(userService.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.login(new LoginRequest("ghost", "whatever")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
