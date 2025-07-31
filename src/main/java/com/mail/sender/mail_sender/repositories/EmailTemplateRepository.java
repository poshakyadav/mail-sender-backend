package com.mail.sender.mail_sender.repositories;

import com.mail.sender.mail_sender.models.EmailTemplate;
import com.mail.sender.mail_sender.models.User;
import com.mail.sender.mail_sender.utils.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    List<EmailTemplate> findByUser(User user); // Get user-specific templates

    List<EmailTemplate> findByType(TemplateType type); // Get default templates
}
