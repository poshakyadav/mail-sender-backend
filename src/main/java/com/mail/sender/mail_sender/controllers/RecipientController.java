package com.mail.sender.mail_sender.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.mail.sender.mail_sender.models.RecipientList;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.RecipientListRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.repositories.RecipientRepository;
import com.mail.sender.mail_sender.services.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recipients")
public class RecipientController {

    private final RecipientRepository recipientRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RecipientListRepository recipientListRepository;

    public RecipientController(RecipientRepository recipientRepository, JwtService jwtService,
            UserRepository userRepository, RecipientListRepository recipientListRepository) {
        this.recipientRepository = recipientRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.recipientListRepository = recipientListRepository;
    }

    @PostMapping("/{recipientListId}")
    public ResponseEntity<Recipient> addRecipient(
            @PathVariable Long recipientListId,
            @RequestBody @Valid Recipient recipient,
            @RequestHeader("Authorization") String token) {

        // Extract username from JWT token
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        // Fetch user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch the recipient list
        RecipientList recipientList = recipientListRepository.findById(recipientListId)
                .orElseThrow(() -> new EntityNotFoundException("RecipientList not found"));

        if (recipientRepository.findByEmailAndRecipientList(recipient.getEmail(), recipientList).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Prevent duplicate within the same list but allow in different lists
        }

        // Associate the recipient with the user and recipient list
        recipient.setUser(user);
        recipient.setRecipientList(recipientList);
        recipient.setAddedAt(LocalDateTime.now());

        // Save and return the recipient
        return ResponseEntity.ok(recipientRepository.save(recipient));
    }

    @GetMapping
    public ResponseEntity<List<Recipient>> getAllRecipients(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch recipients associated with the user
        return ResponseEntity.ok(recipientRepository.findByUser(user));
    }

    @DeleteMapping("/{recipientId}")
    public ResponseEntity<?> deleteRecipient(
            @PathVariable Long recipientId,
            @RequestHeader("Authorization") String token) {

        // Extract JWT token and username
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        // Retrieve the user based on the username from the token
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Retrieve the recipient by ID
        Optional<Recipient> optionalRecipient = recipientRepository.findById(recipientId);
        if (!optionalRecipient.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipient not found");
        }
        Recipient recipient = optionalRecipient.get();

        // Ensure that the recipient belongs to the authenticated user
        if (!recipient.getUser().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to delete this recipient");
        }

        // Delete the recipient
        recipientRepository.delete(recipient);
        return ResponseEntity.ok("Recipient deleted successfully");
    }

}
