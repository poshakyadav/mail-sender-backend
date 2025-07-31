package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.models.Recipient;
import com.mail.sender.mail_sender.models.UnsubscribedRecipient;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.RecipientRepository;
import com.mail.sender.mail_sender.repositories.UnsubscribedRecipientRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/mail")
public class MailUnsubscribeController {

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnsubscribedRecipientRepository unsubscribedRecipientRepository;


    @GetMapping("/unsubscribe")
    public ModelAndView showUnsubscribeConfirmation(@RequestParam String email, @RequestParam Long userId) {
        ModelAndView modelAndView = new ModelAndView("unsubscribe_confirmation"); // Thymeleaf template
        modelAndView.addObject("email", email);
        modelAndView.addObject("userId", userId);
        return modelAndView;
    }

    @PostMapping("/confirm-unsubscribe")
    public ModelAndView confirmUnsubscribe(@RequestParam String email, @RequestParam Long userId) {
        Recipient recipient = recipientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Unsubscribe only from this specific user
        if (!unsubscribedRecipientRepository.existsByUserAndRecipient(user, recipient)) {
            UnsubscribedRecipient unsubscribed = new UnsubscribedRecipient(user, recipient);
            unsubscribedRecipientRepository.save(unsubscribed);
        }

        ModelAndView modelAndView = new ModelAndView("unsubscribe_success");
        modelAndView.addObject("message", "Unsubscription successful! You will no longer get emails from this sender.");
        return modelAndView;
    }


}
