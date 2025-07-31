package com.mail.sender.mail_sender.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailConfigurationRepository;

@Service
public class EmailConfigurationServiceImpl implements EmailConfigurationService {

    private final EmailConfigurationRepository repository;

    public EmailConfigurationServiceImpl(EmailConfigurationRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmailConfiguration addEmailConfiguration(EmailConfiguration config, User user) {
        if (repository.findByEmailAndUser(config.getEmail(), user).isPresent()) {
            throw new IllegalArgumentException("Email already configured for this user.");
        }
        config.setUser(user);
        config.setActive(false);
        return repository.save(config);
    }

    @Override
    public List<EmailConfiguration> getAllConfigurations(User user) {
        return repository.findByUser(user);
    }

    @Override
    public EmailConfiguration activateConfiguration(Long id, User user) {
        // Deactivate currently active configuration
        repository.findByUserAndIsActiveTrue(user).ifPresent(config -> {
            config.setActive(false);
            repository.save(config);
        });

        // Activate new configuration
        EmailConfiguration config = repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Invalid configuration ID"));
        config.setActive(true);
        return repository.save(config);
    }

    @Override
    public EmailConfiguration getActiveConfiguration(User user) {
        return repository.findByUserAndIsActiveTrue(user)
                // .orElseThrow(() -> new IllegalArgumentException("No active email
                // configuration found."));
                .orElse(null); // Return null if no active configuration is found
    }

    @Override
    public void deleteConfiguration(Long id, User user) {
        EmailConfiguration config = repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));

        // If deleting active config, deactivate first
        if (config.isActive()) {
            config.setActive(false);
            repository.save(config);
        }

        repository.delete(config);
    }

    // ------------------------------------
    // @Override
    // public EmailConfiguration getAvailableConfiguration(User user) {
    // List<EmailConfiguration> configs = repository.findByUser(user);

    // // Reset daily counts if needed
    // configs.forEach(config -> {
    // if (config.getLastResetDate().isBefore(LocalDateTime.now())) {
    // config.setDailySentCount(0);
    // config.setLastResetDate(LocalDateTime.now());
    // repository.save(config);
    // }
    // });

    // return configs.stream()
    // .filter(c -> c.getDailySentCount() < 450)
    // .min(Comparator.comparingInt(EmailConfiguration::getDailySentCount))
    // .orElse(null);
    // }

    @Override
    public EmailConfiguration getAvailableConfiguration(User user) {
        List<EmailConfiguration> configs = repository.findByUser(user);

        // Reset daily counts if needed
        configs.forEach(config -> {
            LocalDateTime lastReset = config.getLastResetDate();
            LocalDate today = LocalDate.now();

            if (lastReset == null) {
                // Initialize if lastResetDate is null
                config.setDailySentCount(0);
                config.setLastResetDate(LocalDateTime.now());
            } else {
                LocalDate lastResetDate = lastReset.toLocalDate();
                if (lastResetDate.isBefore(today)) {
                    // Reset count if last reset was before today
                    config.setDailySentCount(0);
                    config.setLastResetDate(LocalDateTime.now());
                }
            }
            repository.save(config);
        });

        return configs.stream()
                .filter(c -> c.getDailySentCount() < 450 && c.getHourlySentCount() < 95)
                .min(Comparator.comparingInt(EmailConfiguration::getDailySentCount))
                .orElse(null);
    }

}
