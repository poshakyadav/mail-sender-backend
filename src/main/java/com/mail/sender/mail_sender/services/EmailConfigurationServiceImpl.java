package com.mail.sender.mail_sender.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.repositories.EmailConfigurationRepository;

@Service
public class EmailConfigurationServiceImpl implements EmailConfigurationService {

    private final EmailConfigurationRepository repository;

    public EmailConfigurationServiceImpl(EmailConfigurationRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmailConfiguration addEmailConfiguration(EmailConfiguration config) {
        if (repository.findByEmail(config.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already configured.");
        }
        config.setActive(false); // New configurations are inactive by default.
        return repository.save(config);
    }

    @Override
    public List<EmailConfiguration> getAllConfigurations() {
        return repository.findAll();
    }

    @Override
    public EmailConfiguration activateConfiguration(Long id) {
        // Deactivate currently active configuration
        repository.findByIsActiveTrue().ifPresent(config -> {
            config.setActive(false);
            repository.save(config);
        });

        // Activate new configuration
        EmailConfiguration config = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid configuration ID"));
        config.setActive(true);
        return repository.save(config);
    }

    @Override
    public EmailConfiguration getActiveConfiguration() {
        return repository.findByIsActiveTrue().orElseThrow(
                () -> new IllegalArgumentException("No active email configuration found."));
    }
}

