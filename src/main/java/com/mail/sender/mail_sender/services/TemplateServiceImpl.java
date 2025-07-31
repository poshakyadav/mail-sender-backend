package com.mail.sender.mail_sender.services;

import com.mail.sender.mail_sender.exceptions.ResourceNotFoundException;
import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.repositories.EmailTemplateRepository;
import com.mail.sender.mail_sender.utils.TemplateType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    public TemplateServiceImpl(EmailTemplateRepository emailTemplateRepository) {
        this.emailTemplateRepository = emailTemplateRepository;
    }

    @Override
    public EmailTemplate createTemplate(EmailTemplate template, User user) {
        template.setType(TemplateType.DEFAULT); // Only admin creates default templates
        template.setUser(user); // Associate the admin user with the template
        return emailTemplateRepository.save(template);
    }

    @Override
    public List<EmailTemplate> getDefaultTemplates() {
        return emailTemplateRepository.findByType(TemplateType.DEFAULT);
    }

    @Override
    public EmailTemplate getTemplateById(Long id) {
        return emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
    }

    @Override
    public void deleteTemplate(Long id) {
        emailTemplateRepository.deleteById(id);
    }
}