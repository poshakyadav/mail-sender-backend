package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.EmailTemplateCategory;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailTemplateCategoryRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;
import com.mail.sender.mail_sender.services.TemplateService;
import com.mail.sender.mail_sender.utils.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/templates")
public class TemplateController {

    private final TemplateService templateService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailTemplateCategoryRepository categoryRepository; 

    public TemplateController(TemplateService templateService, JwtService jwtService,
                              UserRepository userRepository, EmailTemplateCategoryRepository categoryRepository) {
        this.templateService = templateService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }


    @PostMapping
    public ResponseEntity<EmailTemplate> createTemplate(
            @RequestBody EmailTemplate template,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // Extract the token by removing "Bearer "
        String username = jwtService.extractUsername(jwt);

        // Validate user and ensure they are an admin
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // System.out.println("User Role is :" + user.getRole().equals(Role.ADMIN));
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Access denied. Admin privileges required.");
        }

        // Resolve the category from the provided id
        if (template.getCategory() != null && template.getCategory().getId() != null) {
            EmailTemplateCategory category = categoryRepository.findById(template.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            template.setCategory(category);
        } else {
            throw new IllegalArgumentException("Category is required.");
        }

        // System.out.println("Template to save: " + template);
        EmailTemplate createdTemplate = templateService.createTemplate(template, user);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmailTemplate>> getDefaultTemplates(
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7); // Extract the token
        validateAdmin(jwt);

        List<EmailTemplate> templates = templateService.getDefaultTemplates();
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplate> getTemplateById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7); // Extract the token
        validateAdmin(jwt);

        EmailTemplate template = templateService.getTemplateById(id);
        return new ResponseEntity<>(template, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7); // Extract the token
        validateAdmin(jwt);

        templateService.deleteTemplate(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Helper method to validate if the user is an admin
    private void validateAdmin(String jwt) {
        String username = jwtService.extractUsername(jwt);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Access denied. Admin privileges required.");
        }
    }
}
