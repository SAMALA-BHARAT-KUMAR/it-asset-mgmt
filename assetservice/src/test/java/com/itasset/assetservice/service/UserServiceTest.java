package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.Role;
import com.itasset.assetservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.itasset.assetservice.exception.DuplicateResourceException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Day 16 checkpoint: registerUser stores a BCrypt hash ($2a$...), never the raw password,
// and passwordEncoder.matches(raw, hash) is true.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    // real encoder (not mocked) so we can prove hashing + matching actually work
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void registerUser_hashesPasswordBeforeSaving() {
        UserService service = new UserService(repository, encoder);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = new User();
        user.setUsername("carol");
        user.setRole(Role.EMPLOYEE);

        service.registerUser(user, "s3cret");

        // capture what actually got saved
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        String storedHash = saved.getValue().getPasswordHash();

        assertTrue(storedHash.startsWith("$2a$"), "should store a BCrypt hash");
        assertNotEquals("s3cret", storedHash, "must never store the raw password");
        assertTrue(encoder.matches("s3cret", storedHash), "hash must verify against the raw password");
    }

    @Test
    void registerUser_rejectsDuplicateUsername() {
        UserService service = new UserService(repository, encoder);
        when(repository.existsByUsername("carol")).thenReturn(true);

        User user = new User();
        user.setUsername("carol");

        assertThrows(DuplicateResourceException.class, () -> service.registerUser(user, "s3cret"));
        verify(repository, never()).save(any());
    }
}
