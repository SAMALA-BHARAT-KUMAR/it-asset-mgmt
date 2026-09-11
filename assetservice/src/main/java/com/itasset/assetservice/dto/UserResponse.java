package com.itasset.assetservice.dto;

import com.itasset.assetservice.enums.Role;

// Day 20: the logged-in user's own profile — every User field EXCEPT passwordHash, which never leaves the server
public record UserResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String department,
        Role role) {
}
