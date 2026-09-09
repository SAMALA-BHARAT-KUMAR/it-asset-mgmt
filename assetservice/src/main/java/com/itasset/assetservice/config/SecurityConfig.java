package com.itasset.assetservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Day 16: just the password encoder for now. Day 17 adds the SecurityFilterChain here.
@Configuration
public class SecurityConfig {

    // BCrypt hashes are salted + slow-by-design, so identical passwords get different hashes
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
