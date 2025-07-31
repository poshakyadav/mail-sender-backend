package com.mail.sender.mail_sender.controllers;

import com.mail.sender.mail_sender.dto.UserSettingsDTO;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.UserRepository;
import com.mail.sender.mail_sender.services.JwtService;
import com.mail.sender.mail_sender.services.UserDetailsServiceImp;
import com.mail.sender.mail_sender.utils.ResetStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserDetailsServiceImp userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public UserController(UserDetailsServiceImp userDetailsService, JwtService jwtService,
            UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameExists(@RequestParam String username) {
        boolean exists = userDetailsService.checkUsernameExists(username);
        return ResponseEntity.ok().body(exists);
    }

    @PutMapping("/settings")
    public ResponseEntity<User> updateUserSettings(
            @RequestBody UserSettingsDTO settings,
            @RequestHeader("Authorization") String token) {
        User user = getAuthenticatedUser(token);

        // Ensure only one strategy is active
        // Set strategy based on toggle boolean
        // user.setResetStrategy(settings.isUtcResetEnabled()
        // ? ResetStrategy.UTC_MIDNIGHT
        // : ResetStrategy.LAST_RESET_DATE);

        // Ensure only one strategy is active
        if (settings.isUtcResetEnabled()) {
            System.out.println("Entered UTC_MIDNIGHT block");
            user.setResetStrategy(ResetStrategy.UTC_MIDNIGHT);
        } else {
            System.out.println("Entered LAST_RESET_DATE block");
            user.setResetStrategy(ResetStrategy.LAST_RESET_DATE);
        }

        System.out.println(settings.getResetStrategy());

        // user.setResetStrategy(settings.getResetStrategy());
        user.setAutoSwitchEnabled(settings.isAutoSwitchEnabled());
        user.setTimezone(settings.getTimezone());

        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/settings")
    public ResponseEntity<UserSettingsDTO> getUserSettings(
            @RequestHeader("Authorization") String token) {
        User user = getAuthenticatedUser(token);
        UserSettingsDTO dto = new UserSettingsDTO(
                user.isAutoSwitchEnabled(),
                user.getResetStrategy(),
                user.getTimezone());
        return ResponseEntity.ok(dto);
    }

    // Helper method
    private User getAuthenticatedUser(String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
