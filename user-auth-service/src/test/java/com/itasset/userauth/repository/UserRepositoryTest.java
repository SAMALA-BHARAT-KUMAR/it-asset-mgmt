package com.itasset.userauth.repository;

import com.itasset.userauth.entity.User;
import com.itasset.userauth.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Day 15 checkpoint: the seeded admin is retrievable by username, with the ADMIN role.
@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsSeededAdmin() {
        Optional<User> admin = userRepository.findByUsername("admin");

        assertTrue(admin.isPresent(), "seeded 'admin' user should exist");
        assertEquals(Role.ADMIN, admin.get().getRole());
        assertEquals("Alice Admin", admin.get().getFullName());
    }
}
