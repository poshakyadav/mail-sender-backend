package com.mail.sender.mail_sender.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.models.EmailTemplateCategory;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.EmailTemplateCategoryService;
import com.mail.sender.mail_sender.services.JwtService;

@RestController
@RequestMapping("/categories")
public class PublicEmailTemplateCategoryController {

    private final EmailTemplateCategoryService categoryService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public PublicEmailTemplateCategoryController(EmailTemplateCategoryService categoryService, 
                                                 JwtService jwtService, 
                                                 UserRepository userRepository) {
        this.categoryService = categoryService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/public")
    public ResponseEntity<List<EmailTemplateCategory>> getAllCategoriesForUser(
            @RequestHeader("Authorization") String token) {
        // Remove the "Bearer " part and extract the JWT.
        String jwt = token.substring(7);
        
        // Validate that the user exists (no admin check here).
        String username = jwtService.extractUsername(jwt);
        userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Fetch the categories created by admin.
        List<EmailTemplateCategory> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}
