package com.itasset.assetservice.config;

import com.itasset.assetservice.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Day 16: password encoder. Day 17: SecurityFilterChain skeleton — public vs. private routes.
@Configuration
@EnableMethodSecurity // Day 20: turns on @PreAuthorize checks on controller methods
public class SecurityConfig {

    // BCrypt hashes are salted + slow-by-design, so identical passwords get different hashes
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // The gate: decide which doors are open and which need a valid JWT.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            // stateless REST API -> no server-side session, so CSRF protection isn't needed
            .csrf(csrf -> csrf.disable())
            // Day 19: no sessions at all; identity comes fresh from the JWT on every request
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/hello").permitAll() // public: login/register
                .anyRequest().authenticated())                             // everything else needs a JWT
            // Day 19: the guard runs before Spring's username/password filter, reading the Bearer token
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
