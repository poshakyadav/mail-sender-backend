package com.mail.sender.mail_sender.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailConfigurationRepository;
import com.mail.sender.mail_sender.repositories.UserRepository;

@Component
public class DailyResetScheduler {

    @Autowired
    private EmailConfigurationRepository configRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Resets configurations for users who have chosen the UTC_MIDNIGHT reset strategy.
     * This method runs exactly at midnight UTC (00:00:00 UTC) using a dedicated cron expression.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void resetUtcBasedConfigsAtMidnight() {
        // Fetch all users with the UTC reset strategy.
        List<User> utcUsers = userRepository.findByResetStrategy(ResetStrategy.UTC_MIDNIGHT);
        utcUsers.forEach(user -> {
            user.getEmailConfigurations().forEach(config -> {
                config.setDailySentCount(0);
                config.setLastResetDate(LocalDateTime.now());
            });
        });
        // Save all updated configurations in one batch.
        List<EmailConfiguration> updatedConfigs = utcUsers.stream()
            .flatMap(u -> u.getEmailConfigurations().stream())
            .collect(Collectors.toList());
        configRepository.saveAll(updatedConfigs);
        System.out.println("UTC-based configurations reset at midnight.");
    }

    /**
     * Resets configurations for users using the LAST_RESET_DATE (rolling 24h) reset strategy.
     * This method runs every hour and checks if 24 hours have passed since the last reset.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void resetLastResetDateConfigs() {
        // Retrieve all configurations.
        List<EmailConfiguration> configs = configRepository.findAll();
        configs.forEach(config -> {
            if (config.getUser().getResetStrategy() == ResetStrategy.LAST_RESET_DATE) {
                Duration duration = Duration.between(config.getLastResetDate(), LocalDateTime.now());
                if (duration.toHours() >= 24) {
                    config.setDailySentCount(0);
                    config.setLastResetDate(LocalDateTime.now());
                }
            }
        });
        configRepository.saveAll(configs);
        System.out.println("Configurations with LAST_RESET_DATE strategy reset if 24 hours passed.");
    }
}
