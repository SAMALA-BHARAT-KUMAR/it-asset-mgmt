package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // logout: kill the token so it can't be used again (must run in a transaction)
    void deleteByToken(String token);
}
