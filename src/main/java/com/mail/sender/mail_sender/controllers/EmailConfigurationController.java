package com.mail.sender.mail_sender.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.EmailConfigurationService;
import com.mail.sender.mail_sender.services.EmailServiceImpl;
import com.mail.sender.mail_sender.services.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/email-configurations")
public class EmailConfigurationController {

    private final EmailConfigurationService service;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailServiceImpl emailServiceImpl;

    public EmailConfigurationController(EmailConfigurationService service, JwtService jwtService,
            UserRepository userRepository, EmailServiceImpl emailServiceImpl) {
        this.service = service;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.emailServiceImpl = emailServiceImpl;
    }

    @PostMapping
    public ResponseEntity<EmailConfiguration> addConfiguration(
            @RequestBody @Valid EmailConfiguration config,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(service.addEmailConfiguration(config, user));
    }

    @GetMapping
    public ResponseEntity<List<EmailConfiguration>> getAllConfigurations(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(service.getAllConfigurations(user));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<EmailConfiguration> activateConfiguration(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(service.activateConfiguration(id, user));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveConfiguration(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        EmailConfiguration activeConfig = service.getActiveConfiguration(user);

        return ResponseEntity.ok(activeConfig);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConfiguration(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        service.deleteConfiguration(id, user);
        emailServiceImpl.evictMailSenderForConfig(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getUsage(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")); // get user from token
        List<EmailConfiguration> configs = service.getAllConfigurations(user);

        Map<String, Object> response = new HashMap<>();
        response.put("totalAvailable", configs.stream()
                .filter(c -> c.getDailySentCount() < 450)
                .count());
        response.put("totalLimit", configs.size() * 450);

        return ResponseEntity.ok(response);
    }

}
