package com.itasset.assetservice.dto;

// what login/register hand back: the token plus a bit of context for the client
public record AuthResponse(String token, String username, String role) {
}
