package com.itasset.assetservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Day 19: the guard. Runs once on every request, checks the "Authorization: Bearer <jwt>" header.
// Valid JWT -> tell Spring who this user is + their role. No token/invalid -> stays anonymous
// (and the SecurityFilterChain then 401s any protected route).
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIX.length());
            if (jwtUtil.isValid(token)) {                        // re-check the signature (no DB)
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth); // "this user is in"
            }
        }
        chain.doFilter(request, response); // always continue; the chain decides allow/401
    }
}
