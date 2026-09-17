package com.itasset.userauth.repository;

import com.itasset.userauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring derives the query from the name: WHERE username = ?
    Optional<User> findByUsername(String username);

    // friendly duplicate checks before saving (the @Column(unique=true) is the DB backstop)
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
