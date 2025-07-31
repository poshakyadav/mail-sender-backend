package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.EmailTemplateCategory;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailTemplateCategoryRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;
import com.mail.sender.mail_sender.services.TemplateService;
import com.mail.sender.mail_sender.services.UserTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/templates")
public class UserTemplateController {

    private final UserTemplateService userTemplateService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TemplateService templateService; // Inject TemplateService for fetching default templates
    // Inject the category repository so we can look up a category by id.
    private final EmailTemplateCategoryRepository categoryRepository;


    public UserTemplateController(UserTemplateService userTemplateService, JwtService jwtService,
                                  UserRepository userRepository, TemplateService templateService,
                                  EmailTemplateCategoryRepository categoryRepository) {
        this.userTemplateService = userTemplateService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.templateService = templateService;
        this.categoryRepository = categoryRepository;
    }


    @PostMapping
    public ResponseEntity<EmailTemplate> createUserTemplate(
            @RequestBody EmailTemplate template,
            @RequestHeader("Authorization") String token) {

        // Resolve the category if provided as an object with an id.
        if (template.getCategory() != null && template.getCategory().getId() != null) {
            EmailTemplateCategory category = categoryRepository.findById(template.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            template.setCategory(category);
        } else {
            throw new IllegalArgumentException("Category is required.");
        }

        User user = getAuthenticatedUser(token);

        EmailTemplate createdTemplate = userTemplateService.createUserTemplate(template, user);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmailTemplate>> getUserTemplates(@RequestHeader("Authorization") String token) {
        User user = getAuthenticatedUser(token);

        List<EmailTemplate> templates = userTemplateService.getUserTemplates(user);
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplate> updateUserTemplate(
            @PathVariable Long id,
            @RequestBody EmailTemplate updatedTemplate,
            @RequestHeader("Authorization") String token) {

        User user = getAuthenticatedUser(token);

        // Ensure the template belongs to the authenticated user
        EmailTemplate updated = userTemplateService.updateUserTemplate(id, updatedTemplate, user);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserTemplate(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        User user = getAuthenticatedUser(token);

        // Ensure the template belongs to the authenticated user
        userTemplateService.deleteUserTemplate(id, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // New API to get default templates for users
    @GetMapping("/default-templates")
    public ResponseEntity<List<EmailTemplate>> getDefaultTemplatesForUsers(@RequestHeader("Authorization") String token) {
        User user = getAuthenticatedUser(token);

        // Fetch the default templates that are accessible to all users
        List<EmailTemplate> templates = templateService.getDefaultTemplates(); // Get default templates from the TemplateService
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    // Helper method to authenticate the user and fetch their details
    private User getAuthenticatedUser(String token) {
        String jwt = token.substring(7); // Remove "Bearer "
        String username = jwtService.extractUsername(jwt);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

