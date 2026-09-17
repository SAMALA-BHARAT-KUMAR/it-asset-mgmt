package com.itasset.userauth.repository;

import com.itasset.userauth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // logout: kill the token so it can't be used again (must run in a transaction)
    void deleteByToken(String token);
}
