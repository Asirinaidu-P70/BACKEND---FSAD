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

                // 🔓 Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // 🔓 Allow ALL workshop reads (important)
                .requestMatchers(HttpMethod.GET, "/api/workshops/**").permitAll()

                // 🔓 TEMP FIX: allow users endpoint (avoids 403)
                .requestMatchers("/api/auth/users/**").permitAll()

                // 👨‍💼 ADMIN only operations
                .requestMatchers(HttpMethod.POST, "/api/workshops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/workshops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/workshops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/workshops/**").hasRole("ADMIN")

                // 👤 Logged-in users
                .requestMatchers("/api/registrations/**").authenticated()

                // 🔐 Everything else
                .anyRequest().permitAll()   // 🔥 changed from authenticated()
            )

            // 🔥 JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}