package com.itasset.assetservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Day 16: password encoder. Day 17: SecurityFilterChain skeleton — public vs. private routes.
@Configuration
public class SecurityConfig {

    // BCrypt hashes are salted + slow-by-design, so identical passwords get different hashes
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // The gate: decide which doors are open and which need a login.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // stateless REST API -> no server-side session, so CSRF protection isn't needed
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/hello").permitAll() // public: not logged in yet
                .anyRequest().authenticated())                             // everything else needs login
            // ponytail: httpBasic so protected routes are reachable at all; Day 18 swaps this for JWT
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
