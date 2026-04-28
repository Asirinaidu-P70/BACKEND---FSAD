package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.RegistrationRequest;
import com.example.demo.model.Registration;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.service.RegistrationService;
import com.example.demo.util.JwtUtil;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createRegistration(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestBody RegistrationRequest request
    ) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing bearer token");
        }

        Long authenticatedUserId = jwtUtil.extractUserId(authorizationHeader.substring(7).trim());

        if (request.getWorkshopId() == null) {
            return ResponseEntity.badRequest().body("workshopId is required");
        }

        Registration registration = new Registration();

        registration.setUserId(authenticatedUserId);
        registration.setWorkshopId(request.getWorkshopId());

        if (request.getRegistrationDate() != null && !request.getRegistrationDate().isBlank()) {
            registration.setRegistrationDate(request.getRegistrationDate());
        } else {
            registration.setRegistrationDate(LocalDateTime.now().toString());
        }

        Registration savedRegistration = registrationService.saveRegistration(registration);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRegistration);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRegistrationsByUser(@PathVariable Long userId) {
        List<Registration> registrations = registrationService.getUserRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping
    public ResponseEntity<?> getAllRegistrations() {
        return ResponseEntity.ok(registrationRepository.findAll());
    }
}
