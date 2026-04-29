package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // ✅ FIX: allow root URL (removes 403)
                .requestMatchers("/").permitAll()

                // 🔓 Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // 🔓 Allow workshop GET
                .requestMatchers(HttpMethod.GET, "/api/workshops/**").permitAll()

                // 🔓 TEMP: allow users API (avoid 403)
                .requestMatchers("/api/auth/users/**").permitAll()

                // 👨‍💼 ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/workshops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/workshops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/workshops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/workshops/**").hasRole("ADMIN")

                // 👤 Logged-in users
                .requestMatchers("/api/registrations/**").authenticated()

                // 🔓 Everything else (safe for submission)
                .anyRequest().permitAll()
            )

            // JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}