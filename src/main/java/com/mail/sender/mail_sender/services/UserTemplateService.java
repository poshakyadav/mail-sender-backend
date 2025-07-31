package com.mail.sender.mail_sender.services;

import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.User;

import java.util.List;

public interface UserTemplateService {
    EmailTemplate createUserTemplate(EmailTemplate template, User user);

    List<EmailTemplate> getUserTemplates(User user);

    EmailTemplate updateUserTemplate(Long id, EmailTemplate updatedTemplate, User user);

    void deleteUserTemplate(Long id, User user);
}
