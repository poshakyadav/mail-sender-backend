package com.mail.sender.mail_sender.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mail.sender.mail_sender.models.MailRequest;
import com.mail.sender.mail_sender.models.MailResponse;
import com.mail.sender.mail_sender.services.EmailService;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final EmailService emailService;

    public MailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<MailResponse> sendEmail(@ModelAttribute MailRequest request) {
        MailResponse response = emailService.sendEmail(request);
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
