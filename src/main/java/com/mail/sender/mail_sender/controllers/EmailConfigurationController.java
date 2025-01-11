package com.mail.sender.mail_sender.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.services.EmailConfigurationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/email-configurations")
public class EmailConfigurationController {

    private final EmailConfigurationService service;

    public EmailConfigurationController(EmailConfigurationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmailConfiguration> addConfiguration(@RequestBody @Valid EmailConfiguration config) {
        return ResponseEntity.ok(service.addEmailConfiguration(config));
    }

    @GetMapping
    public ResponseEntity<List<EmailConfiguration>> getAllConfigurations() {
        return ResponseEntity.ok(service.getAllConfigurations());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<EmailConfiguration> activateConfiguration(@PathVariable Long id) {
        return ResponseEntity.ok(service.activateConfiguration(id));
    }

    @GetMapping("/active")
    public ResponseEntity<EmailConfiguration> getActiveConfiguration() {
        return ResponseEntity.ok(service.getActiveConfiguration());
    }
}
