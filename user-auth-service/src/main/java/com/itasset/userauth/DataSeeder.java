package com.itasset.userauth;

import com.itasset.userauth.entity.User;
import com.itasset.userauth.enums.Role;
import com.itasset.userauth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// runs once on startup: seeds one ADMIN so the app (and the ADMIN-only asset endpoints) is testable
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // already seeded? skip inserting, just report — avoids duplicates on every restart
        if (!userRepository.existsByUsername("admin")) {
            seedAdmin();
        }
        // checkpoint: read the count back through the repository
        System.out.printf("Seeded users -> total: %d%n", userRepository.count());
    }

    private void seedAdmin() {
        // dev default; override via ADMIN_SEED_PASSWORD in real environments
        String rawPassword = System.getenv().getOrDefault("ADMIN_SEED_PASSWORD", "admin123");
        User admin = new User();
        admin.setUsername("admin");
        admin.setFullName("Alice Admin");
        admin.setEmail("admin@itasset.local");
        admin.setDepartment("IT");
        admin.setRole(Role.ADMIN);
        // same hashing path the app uses (UserService.registerUser -> passwordEncoder.encode)
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(admin);
    }
}
