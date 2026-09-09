package com.itasset.assetservice.dto;

// body of login/register/refresh: the access token + a bit of context.
// the refresh token is NOT here — it's set as an httpOnly cookie the browser carries automatically.
public record AuthResponse(String accessToken, String username, String role) {
}
