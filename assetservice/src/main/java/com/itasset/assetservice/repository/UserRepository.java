package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring derives the query from the name: WHERE username = ?
    Optional<User> findByUsername(String username);
}
