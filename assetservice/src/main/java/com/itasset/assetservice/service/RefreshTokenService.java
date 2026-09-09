package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.RefreshToken;
import com.itasset.assetservice.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

// Day 18 (refresh): owns the lifecycle of refresh tokens — issue, rotate on use, revoke on logout.
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // login/register -> a fresh random token saved against the user
    @Transactional
    public RefreshToken create(String username) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUsername(username);
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        return repository.save(rt);
    }

    // refresh -> consume the old token (one-time use) and issue a new one. Rotation:
    // a stolen-then-used token is dead the moment the real user refreshes.
    @Transactional
    public RefreshToken rotate(String token) {
        RefreshToken existing = repository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        repository.delete(existing); // old one is spent no matter what
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired, please log in again");
        }
        return create(existing.getUsername());
    }

    // logout -> revoke. Null-safe so a logout without a cookie is a no-op, not an error.
    @Transactional
    public void revoke(String token) {
        if (token != null) {
            repository.deleteByToken(token);
        }
    }
}
