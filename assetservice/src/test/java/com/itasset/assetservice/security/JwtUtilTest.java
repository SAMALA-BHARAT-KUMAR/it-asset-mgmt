package com.itasset.assetservice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Day 21 checkpoint: JwtUtil round-trips username+role, accepts its own fresh token,
// and rejects tampered or expired tokens. Security-critical — isValid() must never be fooled.
class JwtUtilTest {

    // HS256 needs a >= 256-bit (32-byte) key, so this secret is deliberately long
    private static final String SECRET = "this-is-a-very-long-test-secret-key-at-least-32-bytes!!";

    private final JwtUtil jwt = new JwtUtil(SECRET, 60_000); // 60s expiry

    @Test
    void roundTrip_extractsSameUsernameAndRole() {
        String token = jwt.generateToken("bharat", "EMPLOYEE");

        assertEquals("bharat", jwt.extractUsername(token));
        assertEquals("EMPLOYEE", jwt.extractRole(token));
    }

    @Test
    void freshToken_isValid() {
        String token = jwt.generateToken("bharat", "EMPLOYEE");

        assertTrue(jwt.isValid(token));
    }

    @Test
    void tamperedToken_isInvalid() {
        String token = jwt.generateToken("bharat", "EMPLOYEE");
        // flip one char in the payload section -> contents no longer match the signature
        String[] parts = token.split("\\.");
        char first = parts[1].charAt(0);
        String tampered = parts[0] + "." + (first == 'A' ? 'B' : 'A') + parts[1].substring(1) + "." + parts[2];

        assertFalse(jwt.isValid(tampered));
    }

    @Test
    void expiredToken_isInvalid() {
        // negative expiry => the token is born already expired
        JwtUtil expiringJwt = new JwtUtil(SECRET, -1_000);
        String token = expiringJwt.generateToken("bharat", "EMPLOYEE");

        assertFalse(jwt.isValid(token));
    }
}
