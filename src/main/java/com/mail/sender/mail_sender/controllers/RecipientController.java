package com.mail.sender.mail_sender.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.repositories.RecipientRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recipients")
public class RecipientController {

    private final RecipientRepository recipientRepository;

    public RecipientController(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    @PostMapping
    public ResponseEntity<Recipient> addRecipient(@RequestBody @Valid Recipient recipient) {
        if (recipientRepository.findByEmail(recipient.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        recipient.setAddedAt(LocalDateTime.now());
        return ResponseEntity.ok(recipientRepository.save(recipient));
    }

    @GetMapping
    public ResponseEntity<List<Recipient>> getAllRecipients() {
        return ResponseEntity.ok(recipientRepository.findAll());
    }
}
