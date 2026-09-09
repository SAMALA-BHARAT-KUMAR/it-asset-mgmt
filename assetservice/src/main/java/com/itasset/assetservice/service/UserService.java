package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.exception.DuplicateResourceException;
import com.itasset.assetservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER: reject duplicate username/email with a clean 409, hash the password, then save
    public User registerUser(User user, String rawPassword) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + user.getUsername());
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + user.getEmail());
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return repository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
