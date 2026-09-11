package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.UserResponse;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Day 20: "who am I". The JwtAuthFilter already put the identity in the SecurityContext;
// Spring injects it here as `Authentication`, so we just read the username off it — no id in the URL.
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication auth) {
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserResponse(user.getId(), user.getFullName(), user.getUsername(),
                user.getEmail(), user.getDepartment(), user.getRole());
    }
}
