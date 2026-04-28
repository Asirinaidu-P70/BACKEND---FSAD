package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class WorkshopBackendApplication {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(WorkshopBackendApplication.class, args);
    }

    @PostConstruct
    public void initAdminUser() {
        User admin = userRepository.findByEmail("admin@example.com").orElseGet(User::new);

        admin.setName("Administrator");
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");

        if (admin.getPassword() == null || !passwordEncoder.matches("admin123", admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("admin123"));
        }

        userRepository.save(admin);
    }
}
