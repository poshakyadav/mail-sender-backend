package com.mail.sender.mail_sender.services;

import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailTemplateRepository;
import com.mail.sender.mail_sender.utils.TemplateType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTemplateServiceImpl implements UserTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    public UserTemplateServiceImpl(EmailTemplateRepository emailTemplateRepository) {
        this.emailTemplateRepository = emailTemplateRepository;
    }

    @Override
    public EmailTemplate createUserTemplate(EmailTemplate template, User user) {
        template.setUser(user);
        template.setType(TemplateType.USER_CREATED);
        return emailTemplateRepository.save(template);
    }

    @Override
    public List<EmailTemplate> getUserTemplates(User user) {
        return emailTemplateRepository.findByUser(user);
    }

    @Override
    public EmailTemplate updateUserTemplate(Long id, EmailTemplate updatedTemplate, User user) {
        // Find the template by ID and ensure it belongs to the given user
        EmailTemplate existingTemplate = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        if (!existingTemplate.getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied. Template does not belong to the user.");
        }

        // Update the template's fields
        existingTemplate.setName(updatedTemplate.getName());
        existingTemplate.setCategory(updatedTemplate.getCategory());
        existingTemplate.setHtmlContent(updatedTemplate.getHtmlContent());

        return emailTemplateRepository.save(existingTemplate);
    }

    @Override
    public void deleteUserTemplate(Long id, User user) {
        EmailTemplate existingTemplate = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        if (!existingTemplate.getUser().equals(user)) {
            throw new IllegalArgumentException("Access denied. Template does not belong to the user.");
        }

        emailTemplateRepository.delete(existingTemplate);
    }
}
