package com.itasset.userauth;

import com.itasset.userauth.entity.User;
import com.itasset.userauth.enums.Role;
import com.itasset.userauth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// runs once on startup: seeds one ADMIN so the app (and the ADMIN-only asset endpoints) is testable.
// @Profile("!prod"): never seed a default admin in prod — prod admins are provisioned for real.
@Component
@Profile("!prod")
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
        // Password from env; only fall back to a dev default if unset — and warn loudly if so,
        // so a weak well-known credential is never used silently.
        String rawPassword = System.getenv("ADMIN_SEED_PASSWORD");
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = "admin123";
            System.out.println("WARNING: seeding admin with the dev-default password — set ADMIN_SEED_PASSWORD outside dev.");
        }
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
