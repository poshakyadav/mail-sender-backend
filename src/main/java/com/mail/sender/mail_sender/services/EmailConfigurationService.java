package com.mail.sender.mail_sender.services;

import java.util.List;

import com.mail.sender.mail_sender.models.EmailConfiguration;

public interface EmailConfigurationService {
    EmailConfiguration addEmailConfiguration(EmailConfiguration config);
    List<EmailConfiguration> getAllConfigurations();
    EmailConfiguration activateConfiguration(Long id);
    EmailConfiguration getActiveConfiguration();
}
