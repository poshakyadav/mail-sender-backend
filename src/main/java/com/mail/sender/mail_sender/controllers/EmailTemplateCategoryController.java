package com.mail.sender.mail_sender.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.models.EmailTemplateCategory;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.EmailTemplateCategoryService;
import com.mail.sender.mail_sender.services.JwtService;
import com.mail.sender.mail_sender.utils.Role;

@RestController
@RequestMapping("/admin/categories")
public class EmailTemplateCategoryController {

    private final EmailTemplateCategoryService categoryService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public EmailTemplateCategoryController(EmailTemplateCategoryService categoryService, JwtService jwtService,
            UserRepository userRepository) {
        this.categoryService = categoryService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<EmailTemplateCategory> createCategory(
            @RequestBody EmailTemplateCategory category,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        validateAdmin(jwt);
        EmailTemplateCategory createdCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmailTemplateCategory>> getAllCategories(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        validateAdmin(jwt);
        List<EmailTemplateCategory> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Helper method to validate admin privileges
    private void validateAdmin(String jwt) {
        String username = jwtService.extractUsername(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Access denied. Admin privileges required.");
        }
    }

}
