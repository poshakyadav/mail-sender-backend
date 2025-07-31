package com.mail.sender.mail_sender.services;

import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.User;

import java.util.List;

public interface TemplateService {
    EmailTemplate createTemplate(EmailTemplate template, User user);

    List<EmailTemplate> getDefaultTemplates();

    EmailTemplate getTemplateById(Long id);

    void deleteTemplate(Long id);
}
