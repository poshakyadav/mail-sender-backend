package com.mail.sender.mail_sender.services;

import java.util.List;

import com.mail.sender.mail_sender.models.EmailConfiguration;
import com.mail.sender.mail_sender.models.User;

public interface EmailConfigurationService {

    /**
     * Adds a new email configuration for a user.
     *
     * @param config The email configuration details.
     * @param user   The user associated with the configuration.
     * @return The saved email configuration.
     */
    EmailConfiguration addEmailConfiguration(EmailConfiguration config, User user);

    /**
     * Retrieves all email configurations for a user.
     *
     * @param user The user whose configurations are to be fetched.
     * @return A list of email configurations.
     */
    List<EmailConfiguration> getAllConfigurations(User user);

    /**
     * Activates the email configuration with the given ID for a user.
     *
     * @param id   The ID of the email configuration to activate.
     * @param user The user associated with the configuration.
     * @return The activated email configuration.
     */
    EmailConfiguration activateConfiguration(Long id, User user);

    /**
     * Retrieves the active email configuration for a user.
     *
     * @param user The user whose active configuration is to be fetched.
     * @return The active email configuration.
     */
    EmailConfiguration getActiveConfiguration(User user);

    void deleteConfiguration(Long id, User user);

    EmailConfiguration getAvailableConfiguration(User user);
}
